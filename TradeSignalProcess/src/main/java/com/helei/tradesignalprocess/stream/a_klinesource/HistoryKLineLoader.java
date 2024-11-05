package com.helei.tradesignalprocess.stream.a_klinesource;

import com.alibaba.fastjson.JSONArray;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.api.rest.BinanceUContractMarketRestApi;
import com.helei.constants.trade.KLineInterval;
import com.helei.binanceapi.supporter.KLineMapper;
import com.helei.dto.trade.KLine;
import com.helei.tradesignalprocess.config.TradeSignalConfig;
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

    private final transient BinanceWSApiClient binanceWSApiClient;

    private final transient BinanceUContractMarketRestApi binanceUContractMarketRestApi;

    private final transient ExecutorService loadThreadPool;

    private final int limit;


    /**
     * 传入websocket api,通过ws获取k线数据
     * @param limit limit
     * @param binanceWSApiClient binanceWSApiClient
     * @param loadThreadPool loadThreadPool
     */
    public HistoryKLineLoader(
            int limit,
            BinanceWSApiClient binanceWSApiClient,
            ExecutorService loadThreadPool
    ) {
        this(limit, binanceWSApiClient, null, loadThreadPool);
    }

    /**
     * 传入binanceUContractMarketRestApi, 传入restapi，通过http请求获取k线数据
     * @param limit limit
     * @param binanceUContractMarketRestApi binanceUContractMarketRestApi
     * @param loadThreadPool loadThreadPool
     */
    public HistoryKLineLoader(
            int limit,
            BinanceUContractMarketRestApi binanceUContractMarketRestApi,
            ExecutorService loadThreadPool
    ) {
        this(limit, null, binanceUContractMarketRestApi, loadThreadPool);
    }

    public HistoryKLineLoader(
            int limit,
            BinanceWSApiClient binanceWSApiClient,
            BinanceUContractMarketRestApi binanceUContractMarketRestApi,
            ExecutorService loadThreadPool
    ) {
        this.limit = limit;
        this.binanceWSApiClient = binanceWSApiClient;
        this.binanceUContractMarketRestApi = binanceUContractMarketRestApi;
        this.loadThreadPool = loadThreadPool;
        this.semaphore = new Semaphore(TradeSignalConfig.TRADE_SIGNAL_CONFIG.getBatchLoadConcurrent());
    }

    /**
     * 开始加载k线数据
     * @param symbol symbol
     * @param interval interval
     * @param startTime startTime
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

            while (curTimeSecond  <= LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) {

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
     * @param interval interval
     * @param upperSymbol upperSymbol
     * @param curTimeSecond curTimeSecond
     * @return CompletableFuture<List<KLine>>
     */
    private CompletableFuture<List<KLine>> batchLoadKLine(KLineInterval interval, String upperSymbol, long curTimeSecond) {
        return CompletableFuture.supplyAsync(()->{
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

    private CompletableFuture<List<KLine>> batchLoadKLineNetwork(KLineInterval interval, String upperSymbol, long curTimeSecond) {
        if (binanceWSApiClient != null) {
            return binanceWSApiClient
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
        } else if (binanceUContractMarketRestApi != null) {
            return binanceUContractMarketRestApi.queryKLines(upperSymbol, interval, curTimeSecond, null, limit);
        } else {
            log.error("没有设置获取k线的apiClient");
            throw new IllegalArgumentException("没有设置获取k线的apiClient");
        }
    }

    /**
     * 关闭客户端
     */
    public void closeClient() {
        if (binanceWSApiClient != null) {
            log.info("关闭客户端[{}]", binanceWSApiClient.getName());
            binanceWSApiClient.close();
        }
    }
}
