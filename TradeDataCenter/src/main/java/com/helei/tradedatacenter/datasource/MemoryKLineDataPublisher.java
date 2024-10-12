package com.helei.tradedatacenter.datasource;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.api.BinanceWSStreamApi;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamParamKey;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamType;
import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;
import com.helei.tradedatacenter.constants.KLineInterval;
import com.helei.tradedatacenter.dto.SubscribeData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * 订阅k线，分发数据。
 * 1。创建类时自动链接Binance
 * 2.调用addListenKLine会向Binance api请求k线数据推送
 * 3.需要k线数据需要调用registry() 方法注册订阅
 * 最后，2步骤中得到推送的k线数据后，会遍历订阅者进行发送
 */
public class MemoryKLineDataPublisher implements KLineDataPublisher{
    private static final ExecutorService PUBLISH_EXECUTOR = Executors.newFixedThreadPool(1);

    private final BinanceWSStreamApi.StreamCommandBuilder streamCommandBuilder;

    private final ConcurrentMap<String, List<SubscribeData>> subscribeMap = new ConcurrentHashMap<>();

    public MemoryKLineDataPublisher(
            int threadPoolSize,
            String url
    ) throws Exception {
        BinanceWSApiClientClient binanceWSApiClient = CEXApiFactory.binanceApiClient(threadPoolSize, url);
        binanceWSApiClient.connect();

        streamCommandBuilder = binanceWSApiClient
                .getStreamApi()
                .builder();
    }

    /**
     * 获取哪些k线
     * @param symbol symbol
     * @param intervalList intervalList
     * @return  KLineDataPublisher
     */
    @Override
    public MemoryKLineDataPublisher addListenKLine(String symbol, List<KLineInterval> intervalList) {
        intervalList.forEach(kLineInterval -> {
            String key = getKLineMapKey(symbol, kLineInterval);

            streamCommandBuilder
                    .symbol(symbol)
                    .addSubscribeEntity(
                    StreamSubscribeEntity
                            .builder()
                            .symbol(symbol)
                            .subscribeType(WebSocketStreamType.KLINE)
                            .invocationHandler((streamName, result) -> {
                                //分发订阅的k线
                                dispatchKLineData(key, result);
                            })
                            .build()
                            .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, kLineInterval.getDescribe())
                    )
                    .subscribe();
            //TODO subscribe加回调，成功才put
            subscribeMap.putIfAbsent(key, new ArrayList<>());
        });
        return this;
    }

    /**
     * 对订阅者分发k线数据
     * @param key key
     * @param result result
     */
    private void dispatchKLineData(String key, JSONObject result) {
        PUBLISH_EXECUTOR.execute(()->{
            subscribeMap.computeIfPresent(key, (k,v)->{
                for (SubscribeData subscribeData : v) {
                    subscribeData.setData(result);
                }
                return v;
            });
        });
    }


    /**
     * 注册监听k线
     * @param symbol symbol
     * @param interval interval
     * @return SubscribeData
     */
    public SubscribeData registry(String symbol, KLineInterval interval) {
        SubscribeData subscribeData = new SubscribeData();

        subscribeMap.compute(getKLineMapKey(symbol, interval), (k, v) -> {
            if (v == null) { //没有获取这条k线的数据，抛出异常
                throw new IllegalArgumentException("symbol '" + symbol + "' kline interval '" + interval + "' didn't listened");
            }

            //添加监听
            v.add(subscribeData);
            return v;
        });
        return subscribeData;
    }


    /**
     * 计算key
     * @param symbol symbol
     * @param kLineInterval  kLineInterval
     * @return key
     */
    private static String getKLineMapKey(String symbol, KLineInterval kLineInterval) {
        return symbol + "-" + kLineInterval.getDescribe();
    }

}
