package com.helei.tradeapplication.service.impl;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.order.CEXTradeOrder;
import com.helei.tradeapplication.dto.TradeOrderGroup;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.ITradeOrderJDBCService;
import com.helei.tradeapplication.service.TradeOrderCommitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TradeOrderCommitServiceImpl implements TradeOrderCommitService {

    private final ExecutorService executor;


    @Autowired
    private BinanceBaseClientManager binanceBaseClientManager;


    @Autowired
    private ITradeOrderJDBCService tradeOrderJdbcService;


    @Autowired
    public TradeOrderCommitServiceImpl(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getTradeOrderResolveExecutor();
    }

    @Override
    public CompletableFuture<Boolean> traderOrderHandler(RunEnv runEnv, TradeType tradeType, TradeOrderGroup orderGroup) {

        return binanceBaseClientManager
                //Step 1 获取客户端
                .getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.REQUEST_RESPONSE)
                //Step 2 请求发送订单
                .thenApplyAsync(abstractBinanceWSApiClient -> {
                    BinanceWSReqRespApiClient reqRespApiClient = (BinanceWSReqRespApiClient) abstractBinanceWSApiClient;
                    CEXTradeOrder mainOrder = orderGroup.getMainOrder();
                    String clientOrderId = mainOrder.getClientOrderId();

                    //Step 2.1 校验参数
                    ASKey asKey = orderGroup.getAsKey();
                    if (asKey == null || StrUtil.isBlank(asKey.getApiKey()) || StrUtil.isBlank(asKey.getSecretKey())) {
                        throw new IllegalArgumentException(String.format("订单clientId[%s]提交时出错，asKey为空", clientOrderId));
                    }


                    //Step 2 提交主单
                    CompletableFuture<Boolean> mainFuture = reqRespApiClient
                            .getTradeApi()
                            .commitOrder(mainOrder, asKey)
                            .thenApplyAsync(this::updateDBTradeOrderStatus)
                            .exceptionallyAsync(throwable -> {
                                log.error("提交交易订单cex[{}]-runEnv[{}]-tradeType[{}]-clientId[{}]发生错误",
                                        mainOrder.getOriCEXType(), mainOrder.getRunEnv(), mainOrder.getTradeType(), mainOrder.getClientOrderId(), throwable);
                                return false;
                            }, executor);

                    try {
                        if (mainFuture.get()) {
                            log.info("提交交易订单cex[{}]-runEnv[{}]-tradeType[{}]-clientId[{}]成功",
                                    mainOrder.getOriCEXType(), mainOrder.getRunEnv(), mainOrder.getTradeType(), mainOrder.getClientOrderId());
                        } else {
                            throw new RuntimeException("提交主单失败");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("提交交易订单cex[{}]-runEnv[{}]-tradeType[{}]-clientId[{}]发生错误",
                                mainOrder.getOriCEXType(), mainOrder.getRunEnv(), mainOrder.getTradeType(), mainOrder.getClientOrderId(), e);
                        return false;
                    }


                    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

                    //Step 3 提交止损单
                    for (CEXTradeOrder stopOrder : orderGroup.getStopOrders()) {
                        CompletableFuture<Boolean> future = reqRespApiClient
                                .getTradeApi()
                                //Step 3.1 提交
                                .commitOrder(stopOrder, asKey)
                                //Step 3.2 修改数据库状态
                                .thenApplyAsync(this::updateDBTradeOrderStatus, executor)
                                .exceptionallyAsync(throwable -> {
                                    log.error("提交止损订单cex[{}]-runEnv[{}]-tradeType[{}]-clientId[{}]发生错误",
                                            stopOrder.getOriCEXType(), stopOrder.getRunEnv(), stopOrder.getTradeType(), stopOrder.getClientOrderId(), throwable);
                                    return false;
                                }, executor);

                        futures.add(future);
                    }

                    //Step 4 提交止盈单
                    for (CEXTradeOrder profitOrder : orderGroup.getProfitOrders()) {
                        CompletableFuture<Boolean> future = reqRespApiClient
                                .getTradeApi()
                                .commitOrder(profitOrder, asKey)
                                .thenApplyAsync(this::updateDBTradeOrderStatus, executor)
                                .exceptionallyAsync(throwable -> {
                                    log.error("提交止盈订单cex[{}]-runEnv[{}]-tradeType[{}]-clientId[{}]发生错误",
                                            profitOrder.getOriCEXType(), profitOrder.getRunEnv(), profitOrder.getTradeType(), profitOrder.getClientOrderId(), throwable);
                                    return false;
                                }, executor);

                        futures.add(future);
                    }

                    //Step 5 等待提交完成
                    AtomicBoolean res = new AtomicBoolean(true);
                    try {
                        CompletableFuture
                                .allOf(futures.toArray(new CompletableFuture[0]))
                                .whenCompleteAsync((unused, throwable) -> {
                                    if (throwable != null) {
                                        log.error("提交订单组发生错误,[{}]", orderGroup, throwable);
                                    } else {
                                        for (CompletableFuture<Boolean> future : futures) {
                                            try {
                                                Boolean success = future.get();
                                                res.compareAndExchange(BooleanUtil.isFalse(success), false);
                                            } catch (InterruptedException | ExecutionException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                }, executor).get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("提交订单组发生错误,[{}]", orderGroup, e);
                        return false;
                    }

                    return res.get();
                }, executor);
    }


    /**
     * 更新数据库订单状态
     *
     * @param result 提交订单的结果
     * @return 提交结果
     */
    private boolean updateDBTradeOrderStatus(JSONObject result) {
        CEXTradeOrder mainOrderResult = result.toJavaObject(CEXTradeOrder.class);
        return tradeOrderJdbcService.updateById(mainOrderResult);
    }
}



