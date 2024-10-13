package com.helei.tradedatacenter.datasource;

import com.alibaba.fastjson.JSONArray;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.dto.SubscribeData;
import com.helei.tradedatacenter.entity.KLine;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


/**
 * 历史k线数据源
 */
@Slf4j
public class HistoryKLineSource {

    private transient Thread loadThread;

    private transient SubscribeData<List<KLine>> subscribeData;

    final transient BinanceWSApiClient binanceWSApiClient;

    private final String symbol;

    private final KLineInterval interval;

    private long curTimeSecond;

    private final int limit;

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

    public synchronized SubscribeData<List<KLine>> startLoadHistory() throws InterruptedException {
        this.subscribeData = new SubscribeData<>();

        loadThread = new Thread(()->{
            while (curTimeSecond <= LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) {
                CountDownLatch latch = new CountDownLatch(1);
                binanceWSApiClient
                        .getSpotApi()
                        .queryHistoryKLine(symbol, interval, curTimeSecond, limit, (result) -> {
                            JSONArray jsonArray = result.getJSONArray("result");

                            List<KLine> history = jsonArray.stream().map(e->{
                                JSONArray kArr = (JSONArray)e;
                                KLine e1 = KLineMapper.mapJsonArrToKLine(kArr);
                                e1.setSymbol(symbol);
                                return e1;
                            }).collect(Collectors.toList());

                            subscribeData.setData(history);
                            latch.countDown();
                        });

                try {
                    latch.await();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    log.error("CountDownLatch error", e);
                    subscribeData.setData(Collections.emptyList());
                    break;
                }
                curTimeSecond += interval.getSecond() * limit;
            }
            subscribeData.setData(Collections.emptyList());
        });
        loadThread.start();
        return subscribeData;
    }


    public void cancel() {
        if (loadThread != null) {
            loadThread.interrupt();
        }
    }
}
