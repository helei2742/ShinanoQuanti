package com.helei.tradedatacenter.datasource;

import com.alibaba.fastjson.JSONArray;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.entity.KLine;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HistoryKLineSource extends BaseKLineSource {
    private transient Thread loadThread;

    private volatile boolean isRunning = true;

    static LinkedBlockingQueue<KLine> bq = new LinkedBlockingQueue<>();

    final transient BinanceWSApiClient binanceWSApiClient;

    private final String symbol;

    private final KLineInterval interval;

    private long curTimeSecond;

    private int limit;

    public HistoryKLineSource(
            String symbol,
            KLineInterval interval,
            LocalDateTime startTime,
            int limit,
            BinanceWSApiClient binanceWSApiClient
    ) {
        this.symbol = symbol;
        this.interval = interval;
        this.limit = limit;
        this.binanceWSApiClient = binanceWSApiClient;
        curTimeSecond = startTime.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    public synchronized void startLoad() throws InterruptedException {
        if (loadThread == null) {
            loadThread = new Thread(()->{
                while (curTimeSecond <= LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) {
                    CountDownLatch latch = new CountDownLatch(1);

                    binanceWSApiClient
                            .getSpotApi()
                            .queryHistoryKLine(symbol, interval, curTimeSecond, limit, (result) -> {
                                JSONArray jsonArray = result.getJSONArray("result");

                                jsonArray.forEach(e->{
                                    JSONArray kArr = (JSONArray)e;
                                    KLine e1 = KLineMapper.mapJsonArrToKLine(kArr);
                                    e1.setSymbol(symbol);
                                    bq.offer(e1);
                                });
                                latch.countDown();
                            });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        log.error("CountDownLatch error", e);
                    }
                    curTimeSecond += interval.getSecond() * limit;
                }
            });

            loadThread.start();
        }
    }


    @Override
    public void run(SourceContext<KLine> sourceContext) throws Exception {
        while (isRunning) {
            KLine kLine = bq.poll(100, TimeUnit.MILLISECONDS);

            if (kLine != null) {
                sourceContext.collect(kLine);
            }
        }
    }

    @Override
    protected KLine loadKLine() throws Exception {
        return null;
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
