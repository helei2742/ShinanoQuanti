package com.helei.reaktimedatacenter.realtime.impl;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.api.ws.BinanceWSStreamApi;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.cexapi.CEXApiFactory;
import com.helei.constants.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.reaktimedatacenter.realtime.KLineRTDataSyncTask;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

@Slf4j
public class BinanceKLineRTDataSyncTask extends KLineRTDataSyncTask {

    /**
     * 流api 获取流实时数据
     */
    private final BinanceWSApiClient streamApiClient;


    private final String url;


    public BinanceKLineRTDataSyncTask(
            List<Pair<String, KLineInterval>> listenKLines,
            String url
    ) {
        super(listenKLines);
        this.url = url;
        streamApiClient = CEXApiFactory.binanceApiClient(url, "币安k线实时获取任务-" + UUID.randomUUID().toString().substring(0, 8));
    }


    public CompletableFuture<Void> startSync(
            BiConsumer<String, JSONObject> whenReceiveKLineData,
            ExecutorService taskExecutor
    ) {
        return CompletableFuture.runAsync(() -> {
            CompletableFuture<Void> connect = null;
            try {
                connect = streamApiClient.connect();
                connect.get();
                log.info("连接[{}]成功，开始获取请求实时k线数据", url);

                BinanceWSStreamApi.StreamCommandBuilder streamCommandBuilder = streamApiClient.getStreamApi().builder();
                for (Pair<String, KLineInterval> kLine : listenKLines) {
                    streamCommandBuilder.addSubscribeEntity(
                            StreamSubscribeEntity
                                    .builder()
                                    .symbol(kLine.getKey().toLowerCase())
                                    .subscribeType(WebSocketStreamType.KLINE)
                                    .invocationHandler(whenReceiveKLineData::accept)
                                    .callbackExecutor(taskExecutor)
                                    .build()
                                    .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, kLine.getValue().getDescribe())
                    );
                }

                streamCommandBuilder.subscribe();

                log.info("已发送k线获取请求到api [{}] ", url);

            } catch (SSLException | URISyntaxException | ExecutionException | InterruptedException e) {
                log.error("开始同步k线数据发生错误，连接api[{}]错误", url);
                throw new RuntimeException(e);
            }
        }, taskExecutor);
    }
}
