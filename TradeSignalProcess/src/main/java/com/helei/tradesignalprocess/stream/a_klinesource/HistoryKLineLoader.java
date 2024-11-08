package com.helei.tradesignalprocess.stream.a_klinesource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.api.rest.BinanceRestHttpApiClient;
import com.helei.binanceapi.constants.api.BinanceRestApiType;
import com.helei.constants.RunEnv;
import com.helei.constants.api.RestApiParamKey;
import com.helei.constants.trade.KLineInterval;
import com.helei.binanceapi.supporter.KLineMapper;
import com.helei.constants.trade.TradeType;
import com.helei.dto.trade.KLine;
import com.helei.tradesignalprocess.config.TradeSignalConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 历史k线数据源
 */
@Slf4j
public class HistoryKLineLoader {

    private final transient Semaphore semaphore;

    private final transient BinanceWSReqRespApiClient reqRespApiClient;

    private final transient BinanceRestHttpApiClient binanceRestHttpApiClient;

    private final transient ExecutorService loadThreadPool;

    private final int limit;

    @Getter
    @Setter
    private RunEnv runEnv;

    @Getter
    @Setter
    private TradeType tradeType;

    /**
     * 传入websocket api,通过ws获取k线数据
     *
     * @param limit            limit
     * @param reqRespApiClient reqRespApiClient
     * @param loadThreadPool   loadThreadPool
     */
    public HistoryKLineLoader(
            int limit,
            BinanceWSReqRespApiClient reqRespApiClient,
            ExecutorService loadThreadPool
    ) {
        this(limit, reqRespApiClient, null, loadThreadPool);
    }

    /**
     * 传入binanceUContractMarketRestApi, 传入restapi，通过http请求获取k线数据
     *
     * @param limit                    limit
     * @param binanceRestHttpApiClient binanceRestHttpApiClient
     * @param loadThreadPool           loadThreadPool
     */
    public HistoryKLineLoader(
            int limit,
            BinanceRestHttpApiClient binanceRestHttpApiClient,
            ExecutorService loadThreadPool
    ) {
        this(limit, null, binanceRestHttpApiClient, loadThreadPool);
    }

    public HistoryKLineLoader(
            int limit,
            BinanceWSReqRespApiClient reqRespApiClient,
            BinanceRestHttpApiClient binanceRestHttpApiClient,
            ExecutorService loadThreadPool
    ) {
        this.limit = limit;
        this.reqRespApiClient = reqRespApiClient;
        this.binanceRestHttpApiClient = binanceRestHttpApiClient;
        this.loadThreadPool = loadThreadPool;
        this.semaphore = new Semaphore(TradeSignalConfig.TRADE_SIGNAL_CONFIG.getBatchLoadConcurrent());
    }

    /**
     * 开始加载k线数据
     *
     * @param symbol             symbol
     * @param interval           interval
     * @param startTime          startTime
     * @param batchKLineConsumer batchKLineConsumer
     * @return 最后完成的时间
     */
    public CompletableFuture<Long> startLoad(
            String symbol,
            KLineInterval interval,
            long startTime,
            Consumer<List<KLine>> batchKLineConsumer
    ) {
        String upperSymbol = symbol.toUpperCase();
        return CompletableFuture.supplyAsync(() -> {
            LinkedList<CompletableFuture<List<KLine>>> waitWindow = new LinkedList<>();

            long curTimeSecond = (long) (startTime / 1000.0);

            while (curTimeSecond <= LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) {

                CompletableFuture<List<KLine>> batchLoadFuture = batchLoadKLine(interval, upperSymbol, curTimeSecond);

                waitWindow.add(batchLoadFuture);
                curTimeSecond += interval.getSecond() * limit;
            }

            Iterator<CompletableFuture<List<KLine>>> iterator = waitWindow.iterator();
            while (iterator.hasNext()) {
                CompletableFuture<List<KLine>> batchLoadFuture = iterator.next();
                List<KLine> lines = batchLoadFuture.join();
                batchKLineConsumer.accept(lines);
                iterator.remove();
            }

            return curTimeSecond;
        }, loadThreadPool);
    }

    /**
     * 批加载k线数据
     *
     * @param interval      interval
     * @param upperSymbol   upperSymbol
     * @param curTimeSecond curTimeSecond
     * @return CompletableFuture<List < KLine>>
     */
    private CompletableFuture<List<KLine>> batchLoadKLine(KLineInterval interval, String upperSymbol, long curTimeSecond) {
        return CompletableFuture.supplyAsync(() -> {
            //控制网络并发
            try {
                semaphore.acquire();
                return batchLoadKLineNetwork(interval, upperSymbol, curTimeSecond).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                semaphore.release();
            }
        }, loadThreadPool);
    }

    /**
     * 批加载k线数据网络请求
     * <p>
     * 会根据HistoryKLineLoader初始化传入的客户端类别来选择使用不同的客户端来请求数据:
     * 1,传入reqRespApiClient，最优先，ws请求响应模式的客户端
     * 2.binanceRestHttpApiClient 其次才是rest客户端
     * </p>
     *
     * @param interval      评率
     * @param upperSymbol   交易对
     * @param curTimeSecond 当前时间
     * @return CompletableFuture<List < KLine>>
     */
    private CompletableFuture<List<KLine>> batchLoadKLineNetwork(KLineInterval interval, String upperSymbol, long curTimeSecond) {
        if (reqRespApiClient != null) {
            return reqRespApiClient
                    .getMarketApi()
                    .queryHistoryKLine(upperSymbol, interval, curTimeSecond, limit)
                    .thenApplyAsync(result -> {
                        JSONArray jsonArray = result.getJSONArray("result");

                        List<KLine> collect = jsonArray.stream().map(e -> {
                            JSONArray kArr = (JSONArray) e;
                            KLine e1 = KLineMapper.mapJsonArrToKLine(kArr);
                            e1.setSymbol(upperSymbol);
                            return e1;
                        }).collect(Collectors.toList());

                        log.debug("history kline [{}]", collect);
                        return collect;
                    }, loadThreadPool);
        } else if (binanceRestHttpApiClient != null) {
            JSONObject params = new JSONObject();
            params.put(RestApiParamKey.symbol, upperSymbol);
            params.put(RestApiParamKey.interval, interval.getDescribe());
            params.put(RestApiParamKey.limit, limit);
            params.put(RestApiParamKey.startTime, curTimeSecond);

            return binanceRestHttpApiClient.queryBinanceApi(runEnv, tradeType, BinanceRestApiType.KLINE, params);
        } else {
            log.error("没有设置获取k线的apiClient");
            throw new IllegalArgumentException("没有设置获取k线的apiClient");
        }
    }

    /**
     * 关闭客户端
     */
    public void closeClient() {
        if (reqRespApiClient != null) {
            log.info("关闭客户端[{}]", reqRespApiClient.getName());
            reqRespApiClient.shutdown();
        }
    }
}
