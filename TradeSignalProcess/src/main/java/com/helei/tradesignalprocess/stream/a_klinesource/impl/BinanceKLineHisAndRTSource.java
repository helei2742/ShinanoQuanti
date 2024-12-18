package com.helei.tradesignalprocess.stream.a_klinesource.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.api.rest.BinanceRestHttpApiClient;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.trade.TradeType;
import com.helei.dto.trade.KLine;
import com.helei.constants.RunEnv;
import com.helei.tradesignalprocess.config.TradeSignalConfig;
import com.helei.tradesignalprocess.stream.a_klinesource.HistoryKLineLoader;
import com.helei.tradesignalprocess.stream.a_klinesource.KLineHisAndRTSource;
import com.helei.tradesignalprocess.stream.a_klinesource.KafkaRealTimeSourceFactory;
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
    private transient ConcurrentHashSet<KLineInterval> historyLoadedIntervals;

    /**
     * 币安基础WS客户端管理器
     */
    private transient BinanceBaseClientManager binanceBaseClientManager;

    /**
     * binance api config
     */
    private final BinanceApiConfig binanceApiConfig;



    public BinanceKLineHisAndRTSource(
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
        // TODO 线程池管理
        executor = Executors.newVirtualThreadPerTaskExecutor();
        binanceBaseClientManager = CEXApiFactory.binanceBaseWSClientManager(TradeSignalConfig.TRADE_SIGNAL_CONFIG.getRun_type(), executor);
    }

    @Override
    public void loadDataInBuffer(BlockingQueue<KLine> buffer) {

        int batchSize = tradeSignalConfig.getHistoryKLineBatchSize();
        TradeType tradeType = tradeSignalConfig.getTrade_type();
        Instant startInstant = Instant.ofEpochMilli(startTime);


        //Step 1: 初始化HistoryKLineLoader
        HistoryKLineLoader historyKLineLoader = initHistoryKLineLoader(batchSize, executor);

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
                                kLine.setKLineInterval(interval);
                                buffer.add(kLine);
                            }
                        });
                        try {
                            future.get();
                            log.info("symbol[{}], interval[{}]历史k线数据获取完毕", symbol, interval);
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("加载历史k线数据出错", e);
                        }
                        return interval;
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
                        historyKLineLoader.closeClient();
                        try {
                            close();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        return null;
                    });
        }


        //Step 3: 获取实时k线，写入buffer
        CompletableFuture.runAsync(() -> {
            KafkaRealTimeSourceFactory sourceFactory = new KafkaRealTimeSourceFactory(symbol, intervals);

            KafkaConsumer<String, KLine> rtConsumer = sourceFactory
                    .loadRTKLineStream(BinanceApiConfig.cexType, tradeSignalConfig.getRun_env(), tradeType);

            while (isRunning) {
                ConsumerRecords<String, KLine> records = rtConsumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, KLine> record : records) {
                    KLine kline = record.value();
                    System.out.println(kline);
                    // 只有当历史k线数据加载完毕，才会向写入buffer中加入实时数据
                    KLineInterval kLineInterval = kline.getKLineInterval();
                    if ((kLineInterval != null && historyLoadedIntervals.contains(kLineInterval))) {
                        buffer.add(kline);
                    }
                }
            }
        }, executor).exceptionally(e -> {
            log.error("加载实时数据出错", e);
            System.exit(-1);
            return null;
        });
    }

    /**
     * 初始化历史k线加载器
     *
     * @param batchSize batchSize
     * @param executor  executor
     * @return HistoryKLineLoader
     */
    @NotNull
    private HistoryKLineLoader initHistoryKLineLoader(
            int batchSize,
            ExecutorService executor
    ) {
        HistoryKLineLoader historyKLineLoader;
        try {
            RunEnv runEnv = tradeSignalConfig.getRun_env();
            TradeType tradeType = tradeSignalConfig.getTrade_type();

            historyKLineLoader = switch (tradeType) {
                case SPOT -> spotHistoryKLineLoader(runEnv, tradeType, batchSize, executor);
                case CONTRACT -> contractHistoryKLineLoader(batchSize, executor);
            };

            historyKLineLoader.setRunEnv(runEnv);
            historyKLineLoader.setTradeType(tradeType);
        } catch (Exception e) {
            throw new RuntimeException("获取历史k线数据加载器失败", e);
        }
        return historyKLineLoader;
    }

    /**
     * 获取现货历史k线数据加载器
     *
     * @param batchSize     batchSize
     * @param executor      executor
     * @return HistoryKLineLoader
     */
    @NotNull
    private HistoryKLineLoader spotHistoryKLineLoader(RunEnv runEnv, TradeType tradeType, int batchSize, ExecutorService executor) throws URISyntaxException, SSLException, InterruptedException, ExecutionException {

        AbstractBinanceWSApiClient client = binanceBaseClientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.REQUEST_RESPONSE).get();

        return new HistoryKLineLoader(batchSize, (BinanceWSReqRespApiClient) client, executor);
    }


    /**
     * 获取合约历史k线数据加载器
     * <p>由于合约币安没提供k线数据的websocket的获取方式，所以用的rest api</p>
     *
     * @param batchSize     batchSize
     * @param executor      executor
     * @return HistoryKLineLoader
     */
    @NotNull
    private HistoryKLineLoader contractHistoryKLineLoader(int batchSize, ExecutorService executor) {
        BinanceRestHttpApiClient restHttpApiClient = CEXApiFactory.binanceRestHttpApiClient(binanceApiConfig, executor);

        return new HistoryKLineLoader(batchSize, restHttpApiClient, executor);
    }

}
