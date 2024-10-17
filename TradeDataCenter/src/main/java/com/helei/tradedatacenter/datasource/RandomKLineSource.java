
package com.helei.tradedatacenter.datasource;

import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.entity.KLine;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RandomKLineSource extends BaseKLineSource {

    private final String symbol;

    private final AtomicLong startTimeStamp;

    private final Random random = new Random();

    private final Double maxPrice;

    private final Double minPrice;


    private final AtomicLong  realTimer;
    @Setter
    private boolean isRealTime = false;

    public RandomKLineSource(String symbol, KLineInterval kLineInterval, LocalDateTime startTimeStamp, Double maxPrice, Double minPrice) {
        super(kLineInterval);
        this.symbol = symbol.toUpperCase();

        this.startTimeStamp = new AtomicLong(startTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli());
        realTimer = this.startTimeStamp;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
    }

    @Override
    protected KLine loadKLine() throws Exception {

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
                openTime = startTimeStamp.addAndGet(openTime + plus);
            }
        }

        TimeUnit.MILLISECONDS.sleep(200);

        KLine kLine = new KLine(symbol, nextOpen, nextClose, nextHigh, nextLow, volume, openTime, openTime + plus - 1000, !isRealTime, kLineInterval, new HashMap<>());
        return kLine;
    }
}
