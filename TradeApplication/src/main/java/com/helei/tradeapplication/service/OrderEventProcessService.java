package com.helei.tradeapplication.service;

import com.helei.constants.order.OrderEvent;
import com.helei.constants.order.GroupOrderStatus;
import com.helei.tradeapplication.dto.GroupOrder;
import com.helei.interfaces.CompleteInvocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;


@Slf4j
public abstract class OrderEventProcessService implements OrderService {


    /**
     * 写入db的重试次数
     */
    private static final int WRITE_DB_RETRY_TIMES = 3;

    /**
     * 写入kafka的重试次数
     */
    private static final int WRITE_KAFKA_RETRY_TIMES = 3;


    /**
     * 阻塞队列， 用于存放订单和当前订单的事件
     */
    private final BlockingQueue<OrderProcessTask> eventQueue = new LinkedBlockingQueue<>();


    /**
     * 存放订单回调的map
     */
    private final ConcurrentMap<GroupOrder, CompleteInvocation<GroupOrder>> invocationMap = new ConcurrentHashMap<>();


    /**
     * 记录重试次数的map
     */
    private final ConcurrentMap<GroupOrder, Integer> retryMap = new ConcurrentHashMap<>();


    /**
     * 执行的线程池
     */
    private final ExecutorService executor;


    public OrderEventProcessService(ExecutorService executor) {
        this.executor = executor;
    }


    /**
     * 提交订单事件
     *
     * @param order              订单
     * @param event              订单事件
     * @param completeInvocation 完成的回调函数
     */
    public void submitOrderEvent(GroupOrder order, OrderEvent event, CompleteInvocation<GroupOrder> completeInvocation) {
        invocationMap.compute(order, (k, v) -> {
            submitOrderEvent(order, event);
            return completeInvocation;
        });
    }

    /**
     * 提交订单事件
     *
     * @param order 订单
     * @param event 订单事件
     */
    public void submitOrderEvent(GroupOrder order, OrderEvent event) {
        try {
            eventQueue.put(new OrderProcessTask(order, event));
        } catch (InterruptedException e) {
            log.error("提交订单[{}]事件[{}]失败", order, event, e);
            throw new RuntimeException("提交订单事件失败", e);
        }
    }

    /**
     * 事件处理
     *
     * @param order 订单
     * @param event 事件
     */
    public void processOrderEvent(GroupOrder order, OrderEvent event) {
        log.debug("开始处理订单[{}]的事件[{}]", order, event);

        OrderEvent next = switch (event) {
            case CREATED_ORDER -> createdOrderProcess(order);

            case SEND_TO_DB -> sendToDBProcess(order);
            case SEND_TO_KAFKA -> sendToKafkaProcess(order);

            case SEND_TO_DB_RETRY -> sendToDBRetryProcess(order);
            case SEND_TO_KAFKA_RETRY -> sendToKafkaRetryProcess(order);

            case SEND_TO_DB_FINAL_ERROR -> errorProcess(order, OrderEvent.SEND_TO_DB_FINAL_ERROR);
            case SEND_TO_KAFKA_FINAL_ERROR -> errorProcess(order, OrderEvent.SEND_TO_KAFKA_FINAL_ERROR);
            case UN_SUPPORT_EVENT_ERROR -> errorProcess(order, OrderEvent.UN_SUPPORT_EVENT_ERROR);

            case BALANCE_INSUFFICIENT -> balanceInsufficientProcess(order);

            case COMPLETE -> successProcess(order);
            case ERROR -> errorProcess(order, OrderEvent.ERROR);
            case CANCEL -> cancelProcess(order);
        };

        if (next != null) {
            submitOrderEvent(order, next);
        }

        log.debug("订单[{}]的事件[{}]处理完毕", order, event);
    }


    /**
     * 资金不足的订单处理
     *
     * @param order groupOrder
     * @return 下一个事件
     */
    private OrderEvent balanceInsufficientProcess(GroupOrder order) {
        if (GroupOrderStatus.BALANCE_INSUFFICIENT.equals(order.getGroupOrderStatus())) {
            try {
                //写入数据库
                writeOrder2DB(order);
            } catch (Exception e) {
                //重试
                return OrderEvent.SEND_TO_DB_RETRY;
            }
            //错误事件
            errorProcess(order, OrderEvent.BALANCE_INSUFFICIENT);
        }
        return OrderEvent.UN_SUPPORT_EVENT_ERROR;
    }


    /**
     * 取消订单
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent cancelProcess(GroupOrder order) {
        //TODO 取消订单逻辑，未写入kafka的标记就好，写入kafka的还需要向另外的kafka里写上取消的消息，订单提交服务收到后进行取消
        return null;
    }


    /**
     * 执行成功的事件处理
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent successProcess(GroupOrder order) {

        CompleteInvocation<GroupOrder> invocation = invocationMap.remove(order);

        if (invocation != null) {
            invocation.success(order);
            invocation.finish();
        }

        return null;
    }


    /**
     * 错误事件处理
     *
     * @param order order
     * @param event 时间
     * @return 下一个事件
     */
    private OrderEvent errorProcess(GroupOrder order, OrderEvent event) {

        CompleteInvocation<GroupOrder> invocation = invocationMap.remove(order);

        if (invocation != null) {
            invocation.fail(order, event.name());
            invocation.finish();
        }

        return null;
    }


    /**
     * 发送到kafka错误重试事件处理
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent sendToKafkaRetryProcess(GroupOrder order) {
        if (GroupOrderStatus.WRITE_IN_KAFKA.equals(order.getGroupOrderStatus())) {
            Integer times = retryMap.remove(order);
            times = times == null ? 0 : times;

            //超过重试次数
            if (times > WRITE_KAFKA_RETRY_TIMES) {
                return OrderEvent.SEND_TO_KAFKA_FINAL_ERROR;
            }


            try {
                GroupOrder result = writeOrder2Kafka(order);

                if (result == null) return OrderEvent.CANCEL;

                return OrderEvent.COMPLETE;
            } catch (Exception e) {
                log.error("写入Order[{}]到kafka发生错误,重试次数[{}]", order, times, e);
                retryMap.put(order, times + 1);
                return OrderEvent.SEND_TO_KAFKA_RETRY;
            }
        }

        return OrderEvent.UN_SUPPORT_EVENT_ERROR;
    }


    /**
     * 发送到kafka事件处理
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent sendToKafkaProcess(GroupOrder order) {
        if (GroupOrderStatus.WRITE_IN_DB.equals(order.getGroupOrderStatus())) {
            // 发送kafka
            try {
                order.setGroupOrderStatus(GroupOrderStatus.WRITE_IN_KAFKA);

                GroupOrder result = writeOrder2Kafka(order);

                if (result == null) return OrderEvent.CANCEL;
            } catch (Exception e) {
                log.error("写入Order[{}]到kafka发生错误", order, e);
                return OrderEvent.SEND_TO_KAFKA_RETRY;
            }
            return OrderEvent.COMPLETE;
        }
        return OrderEvent.UN_SUPPORT_EVENT_ERROR;
    }


    /**
     * 发送到DB错误重试事件处理
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent sendToDBRetryProcess(GroupOrder order) {
        GroupOrderStatus groupOrderStatus = order.getGroupOrderStatus();
        if (GroupOrderStatus.WRITE_IN_DB.equals(groupOrderStatus) || GroupOrderStatus.BALANCE_INSUFFICIENT.equals(groupOrderStatus)) {
            Integer times = retryMap.remove(order);
            times = times == null ? 0 : times;

            //超过重试次数
            if (times > WRITE_DB_RETRY_TIMES) {
                return OrderEvent.SEND_TO_DB_FINAL_ERROR;
            }

            try {
                GroupOrder result = writeOrder2DB(order);

                if (result == null) return OrderEvent.CANCEL;

                //资金不足，只写入数据库记录
                if (GroupOrderStatus.BALANCE_INSUFFICIENT.equals(groupOrderStatus)) return OrderEvent.ERROR;

                return OrderEvent.SEND_TO_KAFKA;
            } catch (Exception e) {
                log.error("写入Order[{}]到数据库发生错误, 重试次数[{}]", order, times, e);
                retryMap.put(order, times + 1);
                return OrderEvent.SEND_TO_DB_RETRY;
            }
        }

        return OrderEvent.UN_SUPPORT_EVENT_ERROR;
    }


    /**
     * 发送到DB事件处理
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent sendToDBProcess(GroupOrder order) {
        if (GroupOrderStatus.CREATED.equals(order.getGroupOrderStatus())) {
            // 写数据库
            try {
                order.setGroupOrderStatus(GroupOrderStatus.WRITE_IN_DB);

                GroupOrder result = writeOrder2DB(order);

                if (result == null) return OrderEvent.CANCEL;
            } catch (Exception e) {
                log.error("写入Order[{}]到数据库发生错误", order, e);
                return OrderEvent.SEND_TO_DB_RETRY;
            }
            return OrderEvent.SEND_TO_KAFKA;
        }
        return OrderEvent.UN_SUPPORT_EVENT_ERROR;
    }

    /**
     * 创建订单事件处理
     *
     * @param order order
     * @return 下一个事件
     */
    private OrderEvent createdOrderProcess(GroupOrder order) {
        //订单创建事件
        order.setGroupOrderStatus(GroupOrderStatus.CREATED);
        return OrderEvent.SEND_TO_DB;
    }


    /**
     * 开始处理事件
     */
    public void startProcessEvents() {
        while (!eventQueue.isEmpty()) {
            try {
                OrderProcessTask task = eventQueue.take();

                executor.execute(() -> processOrderEvent(task.getOrder(), task.getOrderEvent()));
            } catch (InterruptedException e) {
                log.error("处理事件时发生错误", e);
            }
        }
    }


    /**
     * 订单处理任务，包含订单信息和订单事件
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderProcessTask {

        /**
         * 订单信息
         */
        private GroupOrder order;

        /**
         * 订单事件
         */
        private OrderEvent orderEvent;
    }


}
