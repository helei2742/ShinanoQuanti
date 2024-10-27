package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class RandomKLineSource extends BaseKLineSource {

    private final AtomicLong startTimeStamp;

    private final Random random = new Random();

    private final Double maxPrice;

    private final Double minPrice;


    private final AtomicLong realTimer;
    @Setter
    private boolean isRealTime = false;

    public RandomKLineSource(
            String symbol,
            List<KLineInterval> kLineInterval,
            LocalDateTime startTimeStamp,
            Double maxPrice,
            Double minPrice
    ) {
        super(kLineInterval, symbol);

        this.startTimeStamp = new AtomicLong(startTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli());
        realTimer = new AtomicLong(startTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli());
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
        long openTime = startTimeStamp.get();

        if (isRealTime) {
            long curTime = realTimer.addAndGet(200);
            if (curTime >= openTime + plus) {
                openTime = startTimeStamp.addAndGet(plus);
            }
        } else {
            openTime = startTimeStamp.addAndGet(plus);
        }

        TimeUnit.MILLISECONDS.sleep(200);

        KLine kLine = new KLine(getSymbol(), nextOpen, nextClose, nextHigh, nextLow, volume, openTime, openTime + plus - 1000, !isRealTime, kLineInterval, new HashMap<>());
        return kLine;
    }

    @Override
    boolean loadData(SourceContext<KLine> sourceContext) throws Exception {

        while (true) {
            for (KLineInterval interval : getIntervals()) {
                KLine kLine = loadKLine(interval);
                sourceContext.collect(kLine);
            }
        }
    }

    @Override
    void refreshState() {

    }
}

