package com.helei.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.config.CexApiUrlConfig;
import com.helei.config.RealtimeBaseConfig;
import com.helei.config.RealtimeContractDataConfig;
import com.helei.constants.KLineInterval;
import com.helei.constants.TradeType;
import com.helei.dto.SymbolKLineInfo;
import com.helei.realtime.impl.BinanceKLineRTDataSyncTask;
import com.helei.service.MarketRealtimeDataService;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class BinanceContractRTDataService implements MarketRealtimeDataService {

    private final VirtualThreadTaskExecutor taskExecutor = new VirtualThreadTaskExecutor();


    @Autowired
    private RealtimeContractDataConfig realtimeContractDataConfig;

    @Autowired
    private CexApiUrlConfig cexApiUrlConfig;

    @Autowired
    private RealtimeBaseConfig realtimeBaseConfig;


    @Autowired
    public KafkaProducerService kafkaProducerService;


    @Override
    public Integer startSyncRealTimeKLine() {

        //Step 1: 解析k线
        List<SymbolKLineInfo> realtimeKLineList = realtimeContractDataConfig.getRealtimeKLineList();

        List<Pair<String, KLineInterval>> intervals = new ArrayList<>();

        for (SymbolKLineInfo symbolKLineInfo : realtimeKLineList) {
            symbolKLineInfo.getIntervals().forEach(interval -> {
                intervals.add(new Pair<>(symbolKLineInfo.getSymbol(), interval));
            });
        }

        //Step 2: 创建topic
        log.info("开始检查并创建所需topic");
        createTopic(intervals);
        log.info("topic创建完毕");


        //Step 3: 分片执行
        List<List<Pair<String, KLineInterval>>> partition = ListUtil.partition(intervals, realtimeContractDataConfig.getClientListenKLineMaxCount());

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (List<Pair<String, KLineInterval>> list : partition) {

                //Step 4: 创建task执行获取
                CompletableFuture<Void> future = new BinanceKLineRTDataSyncTask(
                        list,
                        cexApiUrlConfig.getUContractStreamUrl()
                ).startSync(this::klineDataSyncToKafka, taskExecutor);

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
    public void klineDataSyncToKafka(String symbol, JSONObject data) {
        log.info("收到k线信息 - {}, - {}", symbol, data);
        try {
            kafkaProducerService.sendMessage(
                     KafkaUtil.resolveKafkaTopic("binance", symbol, TradeType.CONTRACT),
                     data.toJSONString()
             ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("保持k线信息到kafka出错，symbol[{}]", symbol, e);
        }
    }

    private void createTopic(List<Pair<String, KLineInterval>> kLines) {
        for (Pair<String, KLineInterval> kLine : kLines) {
            String topic = KafkaUtil.resolveKafkaTopic("binance", KafkaUtil.getKLineStreamName(kLine.getKey(), kLine.getValue()), TradeType.CONTRACT);

            kafkaProducerService.checkAndCreateTopic(
                    topic,
                    realtimeBaseConfig.getKafkaKlineNumPartitions(),
                    realtimeBaseConfig.getKafkaKlineReplicationFactor()
            );
        }
    }
}
