package com.helei.realtimedatacenter.realtime.impl;

import cn.hutool.core.lang.Pair;
import com.helei.binanceapi.BinanceWSMarketStreamClient;
import com.helei.binanceapi.api.ws.BinanceWSStreamApi;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.constants.WebsocketClientStatus;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.realtimedatacenter.realtime.KLineRTDataSyncTask;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class BinanceKLineRTDataSyncTask extends KLineRTDataSyncTask {

    /**
     * 流api 获取流实时数据
     */
    private final BinanceWSMarketStreamClient marketStreamClient;


    /**
     * 币安k线数据同步任务
     *
     * @param marketStreamClient marketStreamClient
     * @param listenKLines       监听的k线列表
     */
    public BinanceKLineRTDataSyncTask(BinanceWSMarketStreamClient marketStreamClient,
                                      List<Pair<String, KLineInterval>> listenKLines
    ) {
        super(listenKLines);
        this.marketStreamClient = marketStreamClient;
    }


    /**
     * 开始同步
     *
     * @param whenReceiveKLineData 当websocket收到消息时的回调，
     * @param taskExecutor         执行的线程池，回调也会通过这个线程池执行
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> startSync(
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService taskExecutor
    ) {
        return CompletableFuture.runAsync(() -> {
            CompletableFuture<Void> connect = null;
            String clientName = marketStreamClient.getName();
            try {

                // 1 连接
                if (!WebsocketClientStatus.RUNNING.equals(marketStreamClient.getClientStatus())) {

                    throw new RuntimeException(String.format("ws客户端[%s]未连接", clientName));
                }

                // 2 构建请求
                BinanceWSStreamApi.StreamCommandBuilder streamCommandBuilder = marketStreamClient.getStreamApi().builder();
                for (Pair<String, KLineInterval> kLine : listenKLines) {
                    streamCommandBuilder.addSubscribeEntity(
                            StreamSubscribeEntity
                                    .builder()
                                    .symbol(kLine.getKey().toLowerCase())
                                    .subscribeType(WebSocketStreamType.KLINE)
                                    .invocationHandler(whenReceiveKLineData)
                                    .callbackExecutor(taskExecutor)
                                    .build()
                                    .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, kLine.getValue().getDescribe())
                    );
                }

                // 3 请求订阅
                streamCommandBuilder.subscribe();

                log.info("已发送k线获取请求到api client [{}] ", clientName);
            } catch (Exception e) {
                log.error("开始同步k线数据发生错误，连接api client[{}]错误", clientName);
                throw new RuntimeException(String.format("同步k线数据发生错误，连接api[%s]错误", clientName), e);
            }
        }, taskExecutor);
    }
}


