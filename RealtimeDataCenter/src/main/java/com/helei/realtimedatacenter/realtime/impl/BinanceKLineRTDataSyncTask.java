package com.helei.realtimedatacenter.realtime.impl;

import cn.hutool.core.lang.Pair;
import com.helei.binanceapi.BinanceWSMarketStreamClient;
import com.helei.binanceapi.api.ws.BinanceWSStreamApi;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.constants.RunEnv;
import com.helei.constants.WebsocketClientStatus;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.constants.trade.TradeType;
import com.helei.realtimedatacenter.realtime.KLineRTDataSyncTask;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;


@Slf4j
public class BinanceKLineRTDataSyncTask extends KLineRTDataSyncTask<BinanceWSStreamApi.StreamCommandBuilder> {

    private final RunEnv runEnv;

    private final TradeType tradeType;

    /**
     * 流api 获取流实时数据
     */
    private final BinanceWSMarketStreamClient marketStreamClient;

    /**
     * 是否注册k线
     */
    private final SyncKLineFilter kLineFilter;


    /**
     * 币安k线数据同步任务
     *
     * @param marketStreamClient marketStreamClient
     * @param listenKLines       监听的k线列表
     */
    public BinanceKLineRTDataSyncTask(
            BinanceWSMarketStreamClient marketStreamClient,
            List<Pair<String, KLineInterval>> listenKLines,
            SyncKLineFilter kLineFilter
    ) {
        super(listenKLines);
        this.runEnv = marketStreamClient.getRunEnv();
        this.tradeType = marketStreamClient.getTradeType();
        this.marketStreamClient = marketStreamClient;
        this.kLineFilter = kLineFilter;
    }


    @Override
    protected BinanceWSStreamApi.StreamCommandBuilder startRegistryKLine() {
        String clientName = marketStreamClient.getName();
        // 1 连接
        if (!WebsocketClientStatus.RUNNING.equals(marketStreamClient.getClientStatus())) {

            throw new RuntimeException(String.format("ws客户端[%s]未连接", clientName));
        }

        return marketStreamClient.getStreamApi().builder();
    }

    @Override
    protected String whenRegistryKLine(
            String symbol,
            KLineInterval kLineInterval,
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService taskExecutor,
            BinanceWSStreamApi.StreamCommandBuilder commandBuilder
    ) {

        String key = generateSyncKLineKey(runEnv, tradeType, symbol, kLineInterval);
        if (kLineFilter.filter(runEnv, tradeType, symbol, kLineInterval, key)) {
            log.warn("过滤掉K线[{}]", key);
            return null;
        }

        log.debug("注册k线[{}]", key);
        // 2 构建请求
        commandBuilder.addSubscribeEntity(
                StreamSubscribeEntity
                        .builder()
                        .symbol(symbol.toLowerCase())
                        .subscribeType(WebSocketStreamType.KLINE)
                        .invocationHandler(whenReceiveKLineData)
                        .callbackExecutor(taskExecutor)
                        .build()
                        .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, kLineInterval)
        );

        return key;
    }

    @Override
    protected void endRegistryKLine(BinanceWSStreamApi.StreamCommandBuilder commandBuilder) {
        // 3 请求订阅
        commandBuilder.subscribe();
        log.info("已发送k线获取请求到api client [{}] ", marketStreamClient.getName());
    }


    /**
     * 获取当前同步了多少种k线信息
     *
     * @param runEnv        运行环境
     * @param tradeType     交易类型
     * @param symbol        交易对
     * @param kLineInterval k线频率
     * @return key
     */
    private String generateSyncKLineKey(RunEnv runEnv, TradeType tradeType, String symbol, KLineInterval kLineInterval) {
        return String.format("%s.%s.%s.%s", runEnv.name(), tradeType.name(), symbol, kLineInterval.name());
    }


    public interface SyncKLineFilter {
        boolean filter(RunEnv runEnv, TradeType tradeType, String symbol, KLineInterval kLineInterval, String key);
    }
}
