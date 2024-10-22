package com.helei.tradesignalcenter.support;


import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceApiUrl;
import com.helei.constants.KLineInterval;
import com.helei.cexapi.CEXApiFactory;
import com.helei.tradesignalcenter.resolvestream.a_datasource.HistoryKLineLoader;
import com.helei.tradesignalcenter.resolvestream.a_datasource.MemoryKLineDataPublisher;
import com.helei.tradesignalcenter.resolvestream.a_datasource.MemoryKLineSource;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.util.KLineBuffer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KLineTradingDecision {
    private MemoryKLineDataPublisher dataPublisher;

    private MemoryKLineSource memoryKLineSource_btc_15m;
    private MemoryKLineSource memoryKLineSource_btc_2h;

    private MemoryKLineSource memoryKLineSource_eth;

    private String btcusdt = "btcusdt";

    private String ethusdt = "ethusdt";

    BinanceWSApiClient streamClient;
    BinanceWSApiClient normalClient;

    @BeforeAll
    public void before() {
        try {
//            streamClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_SPOT_STREAM_URL);
//            normalClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);
//
//            CompletableFuture.allOf(streamClient.connect(), normalClient.connect()).get();

//            dataPublisher = new MemoryKLineDataPublisher(streamClient, normalClient, 100, 200, 3)
//                    .addListenKLine(btcusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.h_2, KLineInterval.m_15))
//                    .addListenKLine(ethusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.d_1, KLineInterval.m_15));
//
//            memoryKLineSource_btc_2h = new MemoryKLineSource(btcusdt, KLineInterval.h_2, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
//            memoryKLineSource_btc_15m = new MemoryKLineSource(btcusdt, KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
//
//            memoryKLineSource_eth = new MemoryKLineSource(ethusdt, KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    @Qualifier("flinkEnv")
    private StreamExecutionEnvironment env;
    @Autowired
    @Qualifier("flinkEnv2")
    private StreamExecutionEnvironment env2;


    @SneakyThrows
    @Test
    public void testPST() {

    }

    @Test
    public void testHistoryKLineLoader() throws InterruptedException {

        KLineBuffer kb = new KLineBuffer(10);

//        ArrayBlockingQueue<KLine> abq = new ArrayBlockingQueue<>(10);
        AtomicInteger counter = new AtomicInteger();

        new HistoryKLineLoader(200, normalClient, new VirtualThreadTaskExecutor())
                .startLoad("btcusdt", KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli(), kLines -> {
                    System.out.println("get klines count " + kLines.size());
                    for (KLine kLine : kLines) {
                        try {
                            kb.put(kLine);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("add kline " + counter.incrementAndGet() + ", buffer size " + kb.size());
                    }
                }).thenRun(() -> {
                    System.out.println("end of history");
                });

        KLine aline = null;
        while (true) {

            aline = kb.take();
            System.out.println(aline);
            TimeUnit.SECONDS.sleep(1);
            log.info("get line [{}]", aline);
        }

//        TimeUnit.SECONDS.sleep(1000);
    }

}
