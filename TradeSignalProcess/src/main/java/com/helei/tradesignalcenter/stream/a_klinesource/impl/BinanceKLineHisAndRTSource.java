package com.helei.tradesignalcenter.stream.a_klinesource.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.api.rest.BinanceUContractMarketRestApi;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.cexapi.CEXApiFactory;
import com.helei.constants.KLineInterval;
import com.helei.constants.TradeType;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.constants.RunEnv;
import com.helei.tradesignalcenter.stream.a_datasource.HistoryKLineLoader;
import com.helei.tradesignalcenter.stream.a_klinesource.KLineHisAndRTSource;
import com.helei.tradesignalcenter.stream.a_klinesource.KafkaRealTimeSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.*;


/**
 * 币安历史k线和实时k线Flink数据源
 * <p>一个BinanceKLineHisAndReTSource代表一个交易对（symbol）下的k线数据源， 可以有不同的KLineInterval</p>
 * <p>1.先根据startTime获取历史数据，历史数据获取完毕后才向flink source 中加入实时的数据</p>
 * <p>2.实时数据在启动时就通过KafkaConsumer开始统一获取，只是要等到历史数据获取完毕才写入flink source</p>
 */
@Slf4j
public class BinanceKLineHisAndRTSource extends KLineHisAndRTSource {

    /**
     * 相关回调执行的线程池
     */
    private transient ExecutorService executor;

    /**
     * 已加载完毕的历史k线
     */
    private transient ConcurrentHashSet<String> historyLoadedIntervals;

    /**
     * binance api config
     */
    private final BinanceApiConfig binanceApiConfig;


    protected BinanceKLineHisAndRTSource(
            String symbol,
            Set<KLineInterval> kLineIntervals,
            long startTime
    ) {
        super(symbol, kLineIntervals, startTime);
        this.binanceApiConfig = BinanceApiConfig.INSTANCE;
    }

    @Override
    public void onOpen(Configuration parameters) throws Exception {
        historyLoadedIntervals = new ConcurrentHashSet<>();
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void loadDataInBuffer(BlockingQueue<KLine> buffer) {

        int batchSize = tradeSignalConfig.getHistoryKLineBatchSize();
        TradeType tradeType = tradeSignalConfig.getTrade_type();
        Instant startInstant = Instant.ofEpochMilli(startTime);


        //Step 1: 初始化HistoryKLineLoader
        HistoryKLineLoader historyKLineLoader = initHistoryKLineLoader(tradeType, batchSize, executor);

        //Step 2: 遍历k线频率列表，开始加载历史k线数据
        for (KLineInterval interval : intervals) {
            CompletableFuture
                    .supplyAsync(() -> {
                        //Step 2.1: 获取历史k线，写入sourceContext
                        log.info("开始获取历史k线数据, symbol[{}], interval[{}], startTime[{}]",
                                symbol, interval, startInstant);

                        CompletableFuture<Long> future = historyKLineLoader.startLoad(symbol, interval, startTime, kLines -> {
                            log.info("获取到历史k线批数据 [{}]-[{}]: {}", symbol, interval, kLines.size());
                            for (KLine kLine : kLines) {
                                kLine.setSymbol(symbol);
                                buffer.add(kLine);
                            }
                        });
                        try {
                            future.get();
                            log.info("symbol[{}], interval[{}]历史k线数据获取完毕", symbol, interval);
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("加载历史k线数据出错", e);
                            System.exit(-1);
                        }
                        return interval.getDescribe();
                    }, executor)
                    .thenAcceptAsync(itv -> {
                        //Step 2.2: 历史k线获取完毕后记录状态
                        historyLoadedIntervals.add(itv);

                        if (historyLoadedIntervals.size() == intervals.size()) {
                            //所有历史k线获取完毕，关闭客户端
                            log.info("所有历史k线获取完毕");
                            historyKLineLoader.closeClient();
                        }
                    }, executor)
                    .exceptionally(e -> {
                        log.error("异步任务执行异常", e);
                        return null;
                    });
        }


        //Step 3: 获取实时k线，写入buffer
        CompletableFuture.runAsync(()->{
            KafkaRealTimeSourceFactory sourceFactory = new KafkaRealTimeSourceFactory(symbol, intervals);

            KafkaConsumer<String, KLine> rtConsumer = sourceFactory
                    .loadRTKLineStream(BinanceApiConfig.cexType, tradeType);
            while (isRunning) {
                ConsumerRecords<String, KLine> records = rtConsumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, KLine> record : records) {
                    KLine kline = record.value();
                    // 只有当历史k线数据加载完毕，才会向写入buffer中加入实时数据
                    String kLineInterval = kline.getKLineInterval();
                    if ((kLineInterval != null && historyLoadedIntervals.contains(kLineInterval))) {
                        buffer.add(kline);
                    }
                }
            }
        }, executor).exceptionally(e -> {
            log.error("加载实时数据出错", e);
            return null;
        });
    }

    /**
     * 初始化历史k线加载器
     * @param tradeType tradeType
     * @param batchSize batchSize
     * @param executor executor
     * @return HistoryKLineLoader
     */
    @NotNull
    private HistoryKLineLoader initHistoryKLineLoader(
            TradeType tradeType,
            int batchSize,
            ExecutorService executor
    ) {
        String historyApiUrl = getHistoryApiUrl(tradeType);

        HistoryKLineLoader historyKLineLoader;
        log.info("开始初始化历史k线获取客户端, api url[{}]，RunEnv[{}], tradeType[{}]", historyApiUrl, tradeSignalConfig.getRun_env(), tradeType);
        try {
            historyKLineLoader = switch (tradeType) {
                case SPOT -> spotHistoryKLineLoader(historyApiUrl, batchSize, executor);
                case CONTRACT -> contractHistoryKLineLoader(historyApiUrl, batchSize, executor);
            };
        } catch (Exception e) {
            log.error("获取历史k线信息失败，api url [{}]", historyApiUrl, e);
            throw new RuntimeException("获取历史k线信息失败", e);
        }
        return historyKLineLoader;
    }

    /**
     * 获取现货历史k线数据加载器
     * @param historyApiUrl historyApiUrl
     * @param batchSize batchSize
     * @param executor executor
     * @return HistoryKLineLoader
     */
    @NotNull
    private HistoryKLineLoader spotHistoryKLineLoader(String historyApiUrl, int batchSize, ExecutorService executor) throws URISyntaxException, SSLException, InterruptedException, ExecutionException {
        BinanceWSApiClient apiClient = CEXApiFactory.binanceApiClient(historyApiUrl, "binance-history-kline-client");
        apiClient.connect().get();
        return new HistoryKLineLoader(batchSize, apiClient, executor);
    }


    /**
     * 获取合约历史k线数据加载器
     * <p>由于合约币安没提供k线数据的websocket的获取方式，所以用的rest api</p>
     * @param historyApiUrl historyApiUrl
     * @param batchSize batchSize
     * @param executor executor
     * @return HistoryKLineLoader
     */
    @NotNull
    private HistoryKLineLoader contractHistoryKLineLoader(String historyApiUrl, int batchSize, ExecutorService executor) {
        BinanceUContractMarketRestApi binanceUContractMarketRestApi = CEXApiFactory.binanceUContractMarketRestApi(historyApiUrl, executor);
        return new HistoryKLineLoader(batchSize, binanceUContractMarketRestApi, executor);
    }



    /**
     * 获取请求历史数据的api url
     *
     * @return url
     */
    protected String getHistoryApiUrl(TradeType tradeType) {
        if (RunEnv.NORMAL.equals(tradeSignalConfig.getRun_env())) {
            return switch (tradeType) {
                case SPOT -> binanceApiConfig.getNormal().getSpot().getWs_market_url();
                case CONTRACT -> binanceApiConfig.getNormal().getU_contract().getRest_api_url();
            };
        } else {
            return switch (tradeType) {
                case SPOT -> binanceApiConfig.getTest_net().getSpot().getWs_market_url();
                case CONTRACT -> binanceApiConfig.getTest_net().getU_contract().getRest_api_url();
            };
        }
    }
}
