package com.helei.tradedatacenter.datasource;

import com.alibaba.fastjson.JSONArray;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.constants.KLineInterval;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.entity.KLine;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 历史k线数据源
 */
@Slf4j
public class HistoryKLineLoader {

    private final transient BinanceWSApiClient binanceWSApiClient;

    private final int limit;

    private final ExecutorService loadThreadPool;

    public HistoryKLineLoader(
            int limit,
            BinanceWSApiClient binanceWSApiClient,
            ExecutorService loadThreadPool
    ) {
        this.limit = limit;
        this.binanceWSApiClient = binanceWSApiClient;
        this.loadThreadPool = loadThreadPool;

    }

    public CompletableFuture<Long> startLoad(
            String symbol,
            KLineInterval interval,
            LocalDateTime startTime,
            Consumer<List<KLine>> batchKLineConsumer
    ) {
        String upperSymbol = symbol.toUpperCase();
        return CompletableFuture.supplyAsync(() -> {

            long curTimeSecond = startTime.toInstant(ZoneOffset.UTC).getEpochSecond();

            while (curTimeSecond <= LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) {
                CountDownLatch latch = new CountDownLatch(1);

                binanceWSApiClient
                        .getMarketApi()
                        .queryHistoryKLine(upperSymbol, interval, curTimeSecond, limit, (result) -> {
                            JSONArray jsonArray = result.getJSONArray("result");

                            List<KLine> collect = jsonArray.stream().map(e -> {
                                JSONArray kArr = (JSONArray) e;
                                KLine e1 = KLineMapper.mapJsonArrToKLine(kArr);
                                e1.setSymbol(upperSymbol);
                                return e1;
                            }).collect(Collectors.toList());

                            log.debug("history kline [{}]", collect);
                            batchKLineConsumer.accept(collect);

                            latch.countDown();
                        });

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("CountDownLatch error", e);
                    break;
                }
                curTimeSecond += interval.getSecond() * limit;
            }
            return curTimeSecond;
        }, loadThreadPool);
    }
}
