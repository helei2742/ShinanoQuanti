package com.helei.realtimedatacenter.service.impl.account;


import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.binanceapi.dto.accountevent.*;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserAccountRealTimeInfo;
import com.helei.dto.account.UserAccountStaticInfo;
import com.helei.realtimedatacenter.manager.BinanceAccountEventClientManager;
import com.helei.realtimedatacenter.manager.ExecutorServiceManager;
import com.helei.realtimedatacenter.service.AccountEventResolveService;
import com.helei.realtimedatacenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class BinanceAccountEventResolveService implements AccountEventResolveService {


    private ExecutorService eventExecutor = null;

    @Autowired
    private BinanceAccountEventClientManager binanceAccountEventClientManager;

    @Autowired
    private UserService userService;

    @Autowired
    private BinanceBaseClientManager binanceBaseClientManager;


    @Autowired
    public BinanceAccountEventResolveService(ExecutorServiceManager executorServiceManager) {
        this.eventExecutor = executorServiceManager.getAccountEventExecutor();
    }


    @Override
    public void resolveAccountEvent(UserAccountInfo accountInfo, AccountEvent accountEvent) {
        CompletableFuture<Void> future = null;

        switch (accountEvent) {
            case ListenKeyExpireEvent listenKeyExpireEvent ->
                    future = resolveListenKeyExpireEvent(accountInfo, listenKeyExpireEvent);
            case BailNeedEvent bailNeedEvent -> future = resolveBailNeedEvent(accountInfo, bailNeedEvent);
            case BalancePositionUpdateEvent balancePositionUpdateEvent ->
                    future = resolveBalancePositionUpdateEvent(accountInfo, balancePositionUpdateEvent);
            case OrderTradeUpdateLiteEvent orderTradeUpdateEvent ->
                    future = resolveOrderTradeUpdateEvent(orderTradeUpdateEvent);
            case AccountConfigUpdateEvent accountConfigUpdateEvent ->
                    future = resolveAccountConfigUpdateEvent(accountConfigUpdateEvent);
            case StrategyUpdateEvent strategyUpdateEvent -> future = resolveStrategyUpdateEvent(strategyUpdateEvent);
            case GridUpdateEvent gridUpdateEvent -> future = resolveGridUpdateEvent(gridUpdateEvent);
            case ConditionalOrderTriggerRejectEvent conditionalOrderTriggerRejectEvent ->
                    future = resolveConditionalOrderTriggerRejectEvent(conditionalOrderTriggerRejectEvent);
            case null, default ->
                    log.warn("userId[{}]-accountId[{}]-未知事件 [{}]", accountInfo.getUserId(), accountInfo.getId(), accountEvent);
        }


        if (future == null) return;


        future.whenCompleteAsync((unused, throwable) -> {
            if (throwable != null) {
                log.error("处理accountId[{}]事件[{}}发生错误", accountInfo.getId(), accountEvent, throwable);
            }
        }, eventExecutor);
    }

    /**
     * 条件订单(TP/SL)触发后拒绝更新推送
     *
     * @param conditionalOrderTriggerRejectEvent conditionalOrderTriggerRejectEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveConditionalOrderTriggerRejectEvent(ConditionalOrderTriggerRejectEvent conditionalOrderTriggerRejectEvent) {
        return null;
    }


    /**
     * 网格更新推送
     *
     * @param gridUpdateEvent gridUpdateEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveGridUpdateEvent(GridUpdateEvent gridUpdateEvent) {
        return null;
    }

    /**
     * 策略交易更新推送
     *
     * @param strategyUpdateEvent strategyUpdateEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveStrategyUpdateEvent(StrategyUpdateEvent strategyUpdateEvent) {
        return null;
    }


    /**
     * 杠杆倍数等账户配置 更新推送
     *
     * @param accountConfigUpdateEvent accountConfigUpdateEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveAccountConfigUpdateEvent(AccountConfigUpdateEvent accountConfigUpdateEvent) {
        return CompletableFuture.runAsync(() -> {
            //TODO

        }, eventExecutor);
    }

    /**
     * 精简交易推送
     *
     * @param orderTradeUpdateLiteEvent orderTradeUpdateLiteEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveOrderTradeUpdateLiteEvent(OrderTradeUpdateLiteEvent orderTradeUpdateLiteEvent) {
        return CompletableFuture.runAsync(() -> {
            //TODO

        }, eventExecutor);
    }

    /**
     * 订单交易更新推送
     *
     * @param orderTradeUpdateEvent orderTradeUpdateEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveOrderTradeUpdateEvent(OrderTradeUpdateLiteEvent orderTradeUpdateEvent) {
        return CompletableFuture.runAsync(() -> {
            //TODO

        }, eventExecutor);
    }


    /**
     * 处理账户仓位更新事件
     *
     * @param accountInfo                accountInfo
     * @param balancePositionUpdateEvent balancePositionUpdateEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveBalancePositionUpdateEvent(UserAccountInfo accountInfo, BalancePositionUpdateEvent balancePositionUpdateEvent) {

        log.info("account receive balancePositionUpdateEvent [{}}", balancePositionUpdateEvent);
        UserAccountStaticInfo staticInfo = accountInfo.getUserAccountStaticInfo();

        // 收到更新事件，直接请求接口获取最新的仓位信息
        return CompletableFuture.runAsync(() -> {
            binanceBaseClientManager
                    // 1 获取请求客户端
                    .getEnvTypedApiClient(staticInfo.getRunEnv(), staticInfo.getTradeType(), BinanceWSClientType.REQUEST_RESPONSE)
                    // 2 解析结果，更新accountInfo
                    .thenApplyAsync(client -> {
                        try {
                            JSONObject result = ((BinanceWSReqRespApiClient) client).getAccountApi()
                                    .accountStatus(staticInfo.getAsKey(), true)
                                    .get();

                            UserAccountRealTimeInfo realTimeInfo = UserAccountRealTimeInfo.generateAccountStatusFromJson(result);
                            realTimeInfo.setId(staticInfo.getId());
                            realTimeInfo.setUserId(accountInfo.getUserId());

                            return realTimeInfo;
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }, eventExecutor)
                    // 3 发到redis
                    .thenAcceptAsync(realTimeInfo -> {
                        // 3.更新数据库和redis中的信息
                        userService.updateUserAccountRTInfo(staticInfo.getRunEnv(), staticInfo.getTradeType(), realTimeInfo);
                        log.info("accountId[{}]信息更新成功，[{}]", accountInfo.getId(), accountInfo);
                    }, eventExecutor);
        }, eventExecutor);
    }


    /**
     * 处理追加保证金事件
     *
     * @param accountInfo   accountInfo
     * @param bailNeedEvent bailNeedEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveBailNeedEvent(UserAccountInfo accountInfo, BailNeedEvent bailNeedEvent) {
        return CompletableFuture.runAsync(() -> {
            log.warn("账户userId[{}]-accountId[{}]追加保证金事件, 详情:[{}]", accountInfo.getUserId(), accountInfo.getId(), bailNeedEvent);

        }, eventExecutor);
    }


    /**
     * listenKey过期了，要重新获取
     *
     * @param listenKeyExpireEvent listenKeyExpireEvent
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> resolveListenKeyExpireEvent(UserAccountInfo userAccountInfo, ListenKeyExpireEvent listenKeyExpireEvent) {
        long accountId = userAccountInfo.getId();
        log.info("收到accountId[{}]的账户listenKey过期事件，尝试重新连接", accountId);

        return CompletableFuture.runAsync(() -> {
            boolean result = binanceAccountEventClientManager.restartAccountEventStream(accountId);
            if (result) {
                log.info("accountId[{}]的账户重新获取listenKey成功", accountId);
            } else {
                // TODO 日志上传
                log.error("accountId[{}]的账户重新获取listenKey失败", accountId);
            }
        }, eventExecutor);
    }
}

