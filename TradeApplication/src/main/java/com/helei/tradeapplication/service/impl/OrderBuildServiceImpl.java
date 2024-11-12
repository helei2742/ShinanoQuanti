package com.helei.tradeapplication.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.CEXType;
import com.helei.constants.order.GroupOrderStatus;
import com.helei.constants.order.OrderEvent;
import com.helei.constants.order.OrderStatus;
import com.helei.constants.order.OrderType;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.AccountPositionConfig;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.tradeapplication.dto.GroupOrder;
import com.helei.dto.trade.TradeSignal;
import com.helei.interfaces.CompleteInvocation;
import com.helei.tradeapplication.dto.TradeOrderGroup;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.ITradeOrderJDBCService;
import com.helei.tradeapplication.service.OrderEventProcessService;
import com.helei.tradeapplication.supporter.TradeOrderBuildSupporter;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class OrderBuildServiceImpl extends OrderEventProcessService {


    @Autowired
    private KafkaProducerService kafkaProducerService;


    @Autowired
    private ITradeOrderJDBCService binanceContractOrderService;


    @Autowired
    private TradeOrderBuildSupporter tradeOrderBuildSupporter;


    @Autowired
    public OrderBuildServiceImpl(ExecutorServiceManager executorServiceManager) {
        super(executorServiceManager.getOrderExecutor());
        super.startProcessEvents();
    }


    /**
     * 生成订单
     *
     * @param accountInfo   账户信息
     * @param signal        信号
     */
    @Override
    public void makeOrder(UserAccountInfo accountInfo, TradeSignal signal, CompleteInvocation<GroupOrder> invocation) {

        AccountPositionConfig accountPositionConfig = accountInfo.getUserAccountStaticInfo().getAccountPositionConfig();
        OrderType orderType = accountPositionConfig.getOrderType();

        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setAsKey(accountInfo.getUserAccountStaticInfo().getAsKey());

        //Step 1 创建主单
        CEXTradeOrderWrap orderWrapper = switch (orderType) {
            case LIMIT -> tradeOrderBuildSupporter.buildLimitOrder(accountInfo, signal);
            case MARKET -> tradeOrderBuildSupporter.buildMarketOrder(accountInfo, signal);
            default -> null;
        };


        long userId = accountInfo.getId();
        long accountId = accountInfo.getId();

        if (orderWrapper == null) {
            log.warn("userId[{}]-accountId[{}]创建主订单结果为null, signalId[{}]", userId, accountId, signal.getId());
            return;
        }

        // 获取订单
        CEXTradeOrder order = orderWrapper.getFullFieldOrder();

        // 资金不足
        if (order.getQuantity().doubleValue() <= 0) {
            order.setStatus(OrderStatus.BALANCE_INSUFFICIENT);
        }
        groupOrder.setMainOrder(order);


        // 满足资金不足的标记，给group order也打上，不继续创建止盈止损单了
        if (OrderStatus.BALANCE_INSUFFICIENT.equals(order.getStatus())) {
            log.warn("userId[{}]-account[{}] 资金不足，将不会提交订单[{}]到交易所, signalId[{}]", userId, accountId, order.getOrderId(), signal.getId());
            groupOrder.setGroupOrderStatus(GroupOrderStatus.BALANCE_INSUFFICIENT);

            super.submitOrderEvent(groupOrder, OrderEvent.BALANCE_INSUFFICIENT, invocation);
            return;
        }

        //Step 2 根据策略创建止损、止盈单


        log.info("订单[{}]创建成功", order);
        //Step 3 提交订单创建事件
        super.submitOrderEvent(groupOrder, OrderEvent.CREATED_ORDER, invocation);
    }


    @Override
    public GroupOrder writeOrder2Kafka(GroupOrder order) throws ExecutionException, InterruptedException {
        //Step 1 从主单中取出环境信息，生成topic
        BaseOrder mainOrder = order.getMainOrder();

        String topic = KafkaUtil.getOrderSymbolTopic(mainOrder.getOriRunEnv(), mainOrder.getOriTradeType(), mainOrder.getSymbol());

        //Step 2 封装订单对象
        TradeOrderGroup tradeOrderGroup = TradeOrderGroup
                .builder()
                .stopOrders(order.getStopOrders().stream().map(CEXTradeOrderWrap::getFullFieldOrder).toList())
                .profitOrders(order.getProfitOrders().stream().map(CEXTradeOrderWrap::getFullFieldOrder).toList())
                .mainOrder((CEXTradeOrder) order.getMainOrder())
                .build();

        //Step 3 发送kafka
        kafkaProducerService.sendMessage(topic, JSONObject.toJSONString(tradeOrderGroup)).get();

        log.debug("订单order[{}]写入kafka成功", order);

        return order;
    }


    @Override
    public GroupOrder writeOrder2DB(GroupOrder order) {

        BaseOrder mainOrder = order.getMainOrder();
        CEXType cexType = mainOrder.getOriCEXType();
        TradeType tradeType = mainOrder.getOriTradeType();


        if (CEXType.BINANCE.equals(cexType) && TradeType.CONTRACT.equals(tradeType)) {
            List<CEXTradeOrder> cexTradeOrders = binanceContractOrderService.saveGroupOrder(order);
            order.setCexTradeOrders(cexTradeOrders);
            return order;
        }

        return null;
    }

}


