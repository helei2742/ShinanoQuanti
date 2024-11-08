package com.helei.realtimedatacenter.service.impl.market;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.KeyValue;
import com.helei.realtimedatacenter.config.RealtimeConfig;
import com.helei.realtimedatacenter.dto.SymbolKLineInfo;
import com.helei.realtimedatacenter.service.MarketRealtimeDataService;
import com.helei.realtimedatacenter.service.impl.KafkaProducerService;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


/**
 * 市场实时数据服务的抽象类
 * <P>能够将市场数据推送至kafka，会根据配置文件中的run_type来加载需要使用的环境。只需关注实现registryKLineDataLoader(*)方法</P>
 */
@Slf4j
public abstract class AbstractKafkaMarketRTDataService implements MarketRealtimeDataService {
    protected final ExecutorService taskExecutor;

    public final KafkaProducerService kafkaProducerService;

    protected final RealtimeConfig realtimeConfig;

    protected final BinanceApiConfig binanceApiConfig;

    public AbstractKafkaMarketRTDataService(ExecutorService taskExecutor, KafkaProducerService kafkaProducerService) {
        this.taskExecutor = taskExecutor;
        this.kafkaProducerService = kafkaProducerService;
        this.realtimeConfig = RealtimeConfig.INSTANCE;
        this.binanceApiConfig = BinanceApiConfig.INSTANCE;
    }

    @Override
    public Integer startSyncRealTimeKLine() {
        int all = 0;
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        for (KeyValue<RunEnv, TradeType> keyValue : realtimeConfig.getRun_type().getRunTypeList()) {
            futures.add(CompletableFuture.supplyAsync(() -> startSyncRealTimeKLine(keyValue.getKey(), keyValue.getValue()), taskExecutor));
        }

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
        log.info("开始同步env[{}]-tradeType[{}]的实时k线", runEnv, tradeType);

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


        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (List<Pair<String, KLineInterval>> list : partition) {

                //Step 4: 创建task执行获取
                CompletableFuture<Void> future = registryKLineDataLoader(
                        runEnv,
                        tradeType,
                        list,
                        (s, p, k) -> klineDataSyncToKafka(s, (KLineInterval) p.get(WebSocketStreamParamKey.KLINE_INTERVAL), k, runEnv, tradeType),
                        taskExecutor);

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
     * 注册k线数据加载器
     *
     * @param runEnv               运行环境
     * @param tradeType            交易类型
     * @param listenKLines         k线
     * @param whenReceiveKLineData 回调，需要在whenReceiveKLineData.invoke()时传入symbol、interval、json格式的k线数据
     * @param executorService      执行的线程池
     * @return CompletableFuture
     */
    protected abstract CompletableFuture<Void> registryKLineDataLoader(
            RunEnv runEnv,
            TradeType tradeType,
            List<Pair<String, KLineInterval>> listenKLines,
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService executorService
    ) throws ExecutionException, InterruptedException;

    /**
     * 把k线发到kafka
     *
     * @param symbol symbol
     * @param data   data
     */
    public void klineDataSyncToKafka(String symbol, KLineInterval kLineInterval, JSONObject data, RunEnv runEnv, TradeType tradeType) {
        String topic = KafkaUtil.resolveKafkaTopic(CEXType.BINANCE, KafkaUtil.getKLineStreamName(symbol, kLineInterval), runEnv, tradeType);

        log.info("收到k线信息 - {}, - {} - {} - {} send to topic[{}]", symbol, data, runEnv, tradeType, topic);
        try {
            kafkaProducerService.sendMessage(
                    topic,
                    data.toJSONString()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("保持k线信息到kafka出错，symbol[{}]", symbol, e);
        }
    }

    /**
     * 创建topic
     *
     * @param kLines    k线list
     * @param runEnv    运行环境
     * @param tradeType 交易类型
     */
    private void createTopic(List<Pair<String, KLineInterval>> kLines, RunEnv runEnv, TradeType tradeType) {
        for (Pair<String, KLineInterval> kLine : kLines) {
            String topic = KafkaUtil.resolveKafkaTopic(CEXType.BINANCE, KafkaUtil.getKLineStreamName(kLine.getKey(), kLine.getValue()), runEnv, tradeType);

            kafkaProducerService.checkAndCreateTopic(
                    topic,
                    realtimeConfig.getKafka().getKafka_num_partitions(),
                    realtimeConfig.getKafka().getKafka_replication_factor()
            );
        }
    }
}

