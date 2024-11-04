package com.helei.tradeapplication.service.impl;


import com.helei.binanceapi.dto.order.BaseOrder;
import com.helei.binanceapi.dto.order.LimitOrder;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.PositionInfo;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.trade.BalanceInfo;
import com.helei.dto.trade.TradeSignal;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.TradeSignalService;
import com.helei.tradeapplication.service.UserAccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


@Slf4j
@Service
public class KafkaTradeSignalService implements TradeSignalService {

    private final ExecutorService executor;

    @Autowired
    private UserAccountInfoService userAccountInfoService;

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
                    .queryEnvAccountInfo(runEnv, tradeType)
                    .thenApplyAsync(accounts -> makeOrdersAndSend2Trade(runEnv, tradeType, signal, accounts), executor);
        } catch (Exception e) {
            log.error("处理信号[{}]时发生错误", signal, e);
        }

        return true;
    }


    /**
     * 构建订单，符合条件的提交到交易
     *
     * @param runEnv       runEnv
     * @param tradeType    tradeType
     * @param signal       signal
     * @param accountInfos accountInfos
     * @return List<BaseOrder>
     */
    private List<BaseOrder> makeOrdersAndSend2Trade(RunEnv runEnv, TradeType tradeType, TradeSignal signal, List<UserAccountInfo> accountInfos) {

        List<CompletableFuture<BaseOrder>> futures = new ArrayList<>();

        for (UserAccountInfo accountInfo : accountInfos) {
            if (filterAccount(signal, accountInfo)) {
                log.warn("accountId[{}]不能执行信号 [{}]", accountInfo.getId(), signal);
            }

            CompletableFuture<BaseOrder> future = userAccountInfoService
                    .queryAccountRTInfo(runEnv, tradeType, accountInfo.getId())
                    .thenApplyAsync(accountRTData -> {
                        try {
                            return makeOrder(accountInfo, accountRTData, signal);
                        } catch (Exception e) {
                            log.error("为accountId[{}]创建订单时出错, signal[{}]", accountInfo.getId(), signal);
                            return null;
                        }
                    });

            futures.add(future);
        }


        //等待执行完成
        List<BaseOrder> baseOrders = new ArrayList<>();
        for (CompletableFuture<BaseOrder> future : futures) {
            try {
                BaseOrder baseOrder = future.get();
                baseOrders.add(baseOrder);
            } catch (ExecutionException | InterruptedException e) {
                log.error("获取订单结果处理订单结果出错", e);
                throw new RuntimeException(e);
            }
        }
        return baseOrders;
    }


    /**
     * 生成订单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param signal        信号
     * @return 交易的订单
     */
    private BaseOrder makeOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, TradeSignal signal) {
        String symbol = signal.getSymbol().toLowerCase();
        BalanceInfo balanceInfo = accountRTData.getAccountBalanceInfo().getBalances().get(symbol);
        PositionInfo positionInfo = accountRTData.getAccountPositionInfo().getPositions().get(symbol);
        //TODO

        return new LimitOrder();
    }

    /**
     * 根据账户设置过滤
     *
     * @param signal  信号
     * @param account 账户
     * @return List<UserAccountInfo>
     */
    private boolean filterAccount(TradeSignal signal, UserAccountInfo account) {
        return !account.getUsable().get() || !account.getSubscribeSymbol().contains(signal.getSymbol());
    }
}
