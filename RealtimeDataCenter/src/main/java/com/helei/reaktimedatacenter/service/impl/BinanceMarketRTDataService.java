package com.helei.reaktimedatacenter.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.constants.CEXType;
import com.helei.constants.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.reaktimedatacenter.config.RealtimeConfig;
import com.helei.reaktimedatacenter.dto.SymbolKLineInfo;
import com.helei.reaktimedatacenter.manager.ExecutorServiceManager;
import com.helei.reaktimedatacenter.realtime.impl.BinanceKLineRTDataSyncTask;
import com.helei.reaktimedatacenter.service.MarketRealtimeDataService;
import com.helei.util.KafkaUtil;
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
public class BinanceMarketRTDataService implements MarketRealtimeDataService {

    private final ExecutorService taskExecutor;

    private final RealtimeConfig realtimeConfig;

    private final BinanceApiConfig binanceApiConfig;

    @Autowired
    public KafkaProducerService kafkaProducerService;

    @Autowired
    public BinanceMarketRTDataService(ExecutorServiceManager executorServiceManager) {
        this.taskExecutor = executorServiceManager.getKlineTaskExecutor();
        this.realtimeConfig = RealtimeConfig.INSTANCE;
        this.binanceApiConfig = BinanceApiConfig.INSTANCE;
    }


    @Override
    public Integer startSyncRealTimeKLine() {
        int all = 0;
        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        futures.add(CompletableFuture.supplyAsync(() -> startSyncRealTimeKLine(RunEnv.NORMAL, TradeType.SPOT), taskExecutor));
//        futures.add(CompletableFuture.supplyAsync(()->startSyncRealTimeKLine(RunEnv.NORMAL, TradeType.CONTRACT), taskExecutor));
//        futures.add(CompletableFuture.supplyAsync(()->startSyncRealTimeKLine(RunEnv.TEST_NET, TradeType.SPOT), taskExecutor));
//        futures.add(CompletableFuture.supplyAsync(()->startSyncRealTimeKLine(RunEnv.TEST_NET, TradeType.CONTRACT), taskExecutor));

        for (CompletableFuture<Integer> future : futures) {
            try {
                all += future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return all;
    }

    @Override
    public Integer startSyncRealTimeKLine(RunEnv runEnv, TradeType tradeType) {
        RealtimeConfig.RealtimeKLineDataConfig realtimeKLineDataConfig = realtimeConfig.getEnvKLineDataConfig(runEnv, tradeType);

        //Step 1: 解析k线
        List<SymbolKLineInfo> realtimeKLineList = realtimeKLineDataConfig.getRealtimeKLineList();

        if (realtimeKLineList == null || realtimeKLineList.isEmpty()) {
            log.warn("runEnv[{}]-tradeType[{}] 没有设置要实时获取的k线", runEnv, tradeType);
            return 0;
        }

        List<Pair<String, KLineInterval>> intervals = new ArrayList<>();

        for (SymbolKLineInfo symbolKLineInfo : realtimeKLineList) {
            symbolKLineInfo.getIntervals().forEach(interval -> {
                intervals.add(new Pair<>(symbolKLineInfo.getSymbol(), interval));
            });
        }

        //Step 2: 创建topic
        log.info("开始检查并创建所需topic");
        createTopic(intervals, runEnv, tradeType);
        log.info("topic创建完毕");


        //Step 3: 分片执行
        List<List<Pair<String, KLineInterval>>> partition = ListUtil.partition(intervals, realtimeKLineDataConfig.getClient_listen_kline_max_count());

        String url = binanceApiConfig.getEnvUrlSet(runEnv, tradeType).getWs_market_stream_url();

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (List<Pair<String, KLineInterval>> list : partition) {

                //Step 4: 创建task执行获取
                CompletableFuture<Void> future = new BinanceKLineRTDataSyncTask(
                        list,
                        url
                ).startSync((s, d) -> klineDataSyncToKafka(s, d, runEnv, tradeType), taskExecutor);

                futures.add(future);
            }

            CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[0]))
                    .get();
            log.info("所有k线开始实时同步");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return realtimeKLineList.size();
    }

    /**
     * 把k线发到kafka
     *
     * @param symbol symbol
     * @param data   data
     */
    public void klineDataSyncToKafka(String symbol, JSONObject data, RunEnv runEnv, TradeType tradeType) {
        log.info("收到k线信息 - {}, - {} - {} - {}", symbol, data, runEnv, tradeType);
        try {
            kafkaProducerService.sendMessage(
                    KafkaUtil.resolveKafkaTopic(CEXType.BINANCE, symbol, runEnv, tradeType),
                    data.toJSONString()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("保持k线信息到kafka出错，symbol[{}]", symbol, e);
        }
    }

    private void createTopic(List<Pair<String, KLineInterval>> kLines, RunEnv runEnv, TradeType tradeType) {
        for (Pair<String, KLineInterval> kLine : kLines) {
            String topic = KafkaUtil.resolveKafkaTopic(CEXType.BINANCE, KafkaUtil.getKLineStreamName(kLine.getKey(), kLine.getValue()), runEnv, tradeType);

            kafkaProducerService.checkAndCreateTopic(
                    topic,
                    realtimeConfig.getBase().getKafka_kline_num_partitions(),
                    realtimeConfig.getBase().getKafka_kline_replication_factor()
            );
        }
    }
}
