package com.helei.tradesignalcenter.resolvestream.a_datasource;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.resolvestream.a_klinesource.KLineHisAndRTSource;
import lombok.Setter;
import org.apache.flink.configuration.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


public class RandomKLineSource extends KLineHisAndRTSource {
    private transient ExecutorService executorService;

    private final ConcurrentHashMap<KLineInterval, Long> startTimeStampMap;

    private final Random random = new Random();

    private final Double maxPrice;

    private final Double minPrice;

    private final ConcurrentHashMap<KLineInterval, Long> realTimerMap;


    public RandomKLineSource(
            String symbol,
            Set<KLineInterval> kLineInterval,
            LocalDateTime startTimeStamp,
            Double maxPrice,
            Double minPrice
    ) {
        super(symbol, kLineInterval, startTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli());

        long epochMilli = startTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli();
        if (epochMilli > System.currentTimeMillis()) {
            epochMilli = System.currentTimeMillis();
        }
        this.startTimeStampMap = new ConcurrentHashMap<>();
        realTimerMap = new ConcurrentHashMap<>();
        for (KLineInterval lineInterval : kLineInterval) {
            this.startTimeStampMap.put(lineInterval, epochMilli);
            realTimerMap.put(lineInterval, epochMilli);
        }
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
    }

    protected KLine loadKLine(KLineInterval kLineInterval) throws Exception {

        double nextLow = minPrice + (maxPrice - minPrice) * random.nextDouble();
        double nextHigh = nextLow + (maxPrice - nextLow) * random.nextDouble();
        double nextOpen = nextLow + (nextHigh - nextLow) * random.nextDouble();
        double nextClose = nextLow + (nextHigh - nextLow) * random.nextDouble();

        double volume = 10 + (Double.MAX_VALUE / 2 - 10) * random.nextDouble();
        long plus = kLineInterval.getSecond() * 1000;
        long openTime = startTimeStampMap.get(kLineInterval);

        realTimerMap.computeIfPresent(kLineInterval, (k,v)->v + 200);
        long curTime = realTimerMap.get(kLineInterval);

        boolean isRealTime = curTime > System.currentTimeMillis() - kLineInterval.getSecond() * 1000;
        if (isRealTime) {
            if (curTime >= openTime + plus) {
                openTime += plus;
                startTimeStampMap.put(kLineInterval, openTime);
            }
        } else {
            openTime += plus;
            startTimeStampMap.put(kLineInterval, openTime);
        }


        KLine kLine = new KLine(symbol, nextOpen, nextClose, nextHigh, nextLow, volume, openTime,
                openTime + plus - 1000, !isRealTime, kLineInterval, new HashMap<>());
        return kLine;
    }

    @Override
    protected void onOpen(Configuration parameters) throws Exception {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void loadDataInBuffer(BlockingQueue<KLine> buffer) {
        for (KLineInterval interval : intervals) {
            CompletableFuture.runAsync(()->{
                while (true) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        buffer.put(loadKLine(interval));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, executorService);
        }
    }

}

