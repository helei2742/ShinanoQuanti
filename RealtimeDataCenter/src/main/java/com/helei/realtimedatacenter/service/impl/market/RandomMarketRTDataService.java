package com.helei.realtimedatacenter.service.impl.market;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.trade.TradeType;
import com.helei.realtimedatacenter.manager.ExecutorServiceManager;
import com.helei.realtimedatacenter.service.impl.KafkaProducerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
        import java.util.concurrent.*;


/**
 * 随机市场数据服务
 */
@Service
public class RandomMarketRTDataService extends AbstractKafkaMarketRTDataService {

    private final Random random = new Random();

    private final Double maxPrice = 99999.09;

    private final Double minPrice = 111.229;

    private final ConcurrentHashMap<String, ConcurrentHashMap<KLineInterval, Long>> startTimeStampMap;

    private final ConcurrentHashMap<String, ConcurrentHashMap<KLineInterval, Long>> realTimerMap;

    private final LocalDateTime startTimeStamp = LocalDateTime.of(2022, 1, 1, 1, 1);

    private long epochMilli;


    public RandomMarketRTDataService(
            ExecutorServiceManager executorServiceManager, KafkaProducerService kafkaProducerService
    ) {
        super(executorServiceManager.getKlineTaskExecutor(), kafkaProducerService, CEXType.BINANCE);

        epochMilli = startTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli();

        if (epochMilli > System.currentTimeMillis()) {
            epochMilli = System.currentTimeMillis();
        }
        this.startTimeStampMap = new ConcurrentHashMap<>();
        this.realTimerMap = new ConcurrentHashMap<>();
    }

    @Override
    protected CompletableFuture<Set<String>> registryKLineDataLoader(
            RunEnv runEnv,
            TradeType tradeType,
            List<Pair<String, KLineInterval>> listenKLines,
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService executorService
    ) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> set = new HashSet<>();
            String key = getKey(runEnv, tradeType);
            set.add(key);
            for (Pair<String, KLineInterval> listenKLine : listenKLines) {
                String symbol = listenKLine.getKey();
                KLineInterval interval = listenKLine.getValue();

                startTimeStampMap.putIfAbsent(key, new ConcurrentHashMap<>());
                realTimerMap.putIfAbsent(key, new ConcurrentHashMap<>());

                startTimeStampMap.get(key).putIfAbsent(interval, epochMilli);
                realTimerMap.get(key).putIfAbsent(interval, epochMilli);

                executorService.execute(() -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put(WebSocketStreamParamKey.KLINE_INTERVAL, interval);
                    while (true) {
                        try {
                            JSONObject kLine = loadKLine(runEnv, tradeType, symbol, interval);
                            whenReceiveKLineData.invoke(symbol, map, kLine);
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            return set;
        }, executorService);
    }


    protected JSONObject loadKLine(
            RunEnv runEnv,
            TradeType tradeType,
            String symbol,
            KLineInterval kLineInterval
    ) {

        double nextLow = minPrice + (maxPrice - minPrice) * random.nextDouble();
        double nextHigh = nextLow + (maxPrice - nextLow) * random.nextDouble();
        double nextOpen = nextLow + (nextHigh - nextLow) * random.nextDouble();
        double nextClose = nextLow + (nextHigh - nextLow) * random.nextDouble();

        double volume = 10 + (Double.MAX_VALUE / 2 - 10) * random.nextDouble();
        long plus = kLineInterval.getSecond() * 1000;
        String key = getKey(runEnv, tradeType);
        long openTime = startTimeStampMap.get(key).get(kLineInterval);

        realTimerMap.get(key).computeIfPresent(kLineInterval, (k, v) -> v + 200);
        long curTime = realTimerMap.get(key).get(kLineInterval);

        boolean isRealTime = curTime > System.currentTimeMillis() - kLineInterval.getSecond() * 1000;
        if (isRealTime) {
            if (curTime >= openTime + plus) {
                openTime += plus;
                startTimeStampMap.get(key).put(kLineInterval, openTime);
            }
        } else {
            openTime += plus;
            startTimeStampMap.get(key).put(kLineInterval, openTime);
        }

        JSONObject jb = new JSONObject();
        jb.put("t", openTime);
        jb.put("T", openTime + plus - 1000);
        jb.put("s", symbol);
        jb.put("h", nextHigh);
        jb.put("l", nextLow);
        jb.put("o", nextOpen);
        jb.put("c", nextClose);
        jb.put("v", volume);
        jb.put("x", !isRealTime);
        jb.put("i", kLineInterval.name());

        return jb;
    }

    private String getKey(RunEnv runEnv, TradeType tradeType) {
        return runEnv.name() + " - " + tradeType.name();
    }
}
