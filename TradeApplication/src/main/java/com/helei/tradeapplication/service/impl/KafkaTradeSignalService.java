package com.helei.tradeapplication.service.impl;


import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserAccountStaticInfo;
import com.helei.tradeapplication.dto.GroupOrder;
import com.helei.dto.trade.TradeSignal;
import com.helei.interfaces.CompleteInvocation;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.OrderService;
import com.helei.tradeapplication.service.TradeSignalService;
import com.helei.tradeapplication.service.UserAccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


/**
 * 处理交易信号
 */
@Slf4j
@Service
public class KafkaTradeSignalService implements TradeSignalService {

    private final ExecutorService executor;

    @Autowired
    private UserAccountInfoService userAccountInfoService;

    @Autowired
    private OrderService orderService;


    public KafkaTradeSignalService(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getTradeExecutor();
    }


    /**
     * 处理交易信号
     *
     * @param runEnv    运行环境
     * @param tradeType 交易类型
     * @param signal    信号
     * @return true 无论处理结果如何都忽略到改信号
     */
    public boolean resolveTradeSignal(RunEnv runEnv, TradeType tradeType, TradeSignal signal) {

        try {
            userAccountInfoService
                    // 1 查询环境下的账户
                    .queryEnvAccountInfo(runEnv, tradeType)
                    // 2 生成订单并交易
                    .thenApplyAsync(accounts -> makeOrdersAndSend2Trade(runEnv, tradeType, signal, accounts), executor);
        } catch (Exception e) {
            log.error("处理信号[{}]时发生错误", signal, e);
        }

        return true;
    }


    /**
     * 构建订单，符合条件的提交到交易.
     * <p>并不会真正把订单提交到交易所，而是写入数据库后，再写入kafka的topic里</p>
     *
     * @param runEnv       runEnv
     * @param tradeType    tradeType
     * @param signal       signal
     * @param accountInfos accountInfos
     * @return List<BaseOrder>
     */
    private List<GroupOrder> makeOrdersAndSend2Trade(RunEnv runEnv, TradeType tradeType, TradeSignal signal, List<UserAccountInfo> accountInfos) {

        List<CompletableFuture<GroupOrder>> futures = new ArrayList<>();

        for (UserAccountInfo accountInfo : accountInfos) {
            long userId = accountInfo.getUserId();
            long accountId = accountInfo.getId();

            //Step 1 过滤掉账户设置不接受此信号的
            if (filterAccount(signal, accountInfo.getUserAccountStaticInfo())) {
                log.warn("accountId[{}]不能执行信号 [{}]", accountId, signal);
            }

            CompletableFuture<GroupOrder> future = userAccountInfoService
                    //Step 2 查询实时的账户数据
                    .queryAccountNewInfo(runEnv, tradeType, userId, accountId)
                    //Step 3 生产订单
                    .thenApplyAsync(newAccountInfo -> {
                        final GroupOrder[] groupOrder = {null};

                        try {
                            CountDownLatch latch = new CountDownLatch(1);

                            orderService.makeOrder(newAccountInfo, signal, new CompleteInvocation<>() {
                                @Override
                                public void success(GroupOrder order) {
                                    groupOrder[0] = order;
                                    log.info("创建订单[{}]成功", order);
                                }

                                @Override
                                public void fail(GroupOrder order, String errorMsg) {
                                    groupOrder[0] = order;
                                    log.info("创建订单失败[{}],错误原因[{}]", order, errorMsg);
                                }

                                @Override
                                public void finish() {
                                    latch.countDown();
                                }
                            });

                            //等待订单创建完成
                            latch.await();

                        } catch (Exception e) {
                            log.error("为accountId[{}]创建订单时出错, signal[{}]", accountId, signal, e);
                        }
                        return groupOrder[0];
                    })
                    .exceptionallyAsync(throwable -> {
                        if (throwable != null) {
                            log.error("创建订单时发生错误", throwable);
                        }
                        return null;
                    });

            futures.add(future);
        }


        //等待执行完成
        List<GroupOrder> groupOrders = new ArrayList<>();
        for (CompletableFuture<GroupOrder> future : futures) {
            try {
                GroupOrder order = future.get();
                groupOrders.add(order);
            } catch (ExecutionException | InterruptedException e) {
                log.error("获取订单结果处理订单结果出错", e);
                throw new RuntimeException(e);
            }
        }
        return groupOrders;
    }


    /**
     * 根据账户设置过滤
     *
     * @param signal  信号
     * @param staticInfo 账户静态信息
     * @return List<UserAccountInfo>
     */
    private boolean filterAccount(TradeSignal signal, UserAccountStaticInfo staticInfo) {
        return !staticInfo.getUsable().get() || !staticInfo.getSubscribeSymbol().contains(signal.getSymbol());
    }
}


