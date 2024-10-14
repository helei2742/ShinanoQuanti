package com.helei.tradedatacenter.support;


import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.datasource.HistoryKLineLoader;
import com.helei.tradedatacenter.datasource.MemoryKLineDataPublisher;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.calculater.MACDCalculator;
import com.helei.tradedatacenter.indicator.calculater.PSTCalculator;
import com.helei.tradedatacenter.indicator.calculater.RSICalculator;
import com.helei.tradedatacenter.signal.MACDSignal_V1;
import com.helei.tradedatacenter.util.KLineBuffer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KLineTradingDecision {
    private MemoryKLineDataPublisher dataPublisher;

    private MemoryKLineSource memoryKLineSource_btc;

    private MemoryKLineSource memoryKLineSource_eth;

    private String btcusdt = "btcusdt";

    private String ethusdt = "ethusdt";

    BinanceWSApiClient streamClient;
    BinanceWSApiClient normalClient;
    @BeforeAll
    public void before() {
        try {
            streamClient = CEXApiFactory.binanceApiClient(4, WebSocketUrl.WS_STREAM_URL);
            normalClient = CEXApiFactory.binanceApiClient(4, WebSocketUrl.WS_NORMAL_URL);

            streamClient.connect();
            normalClient.connect();

            dataPublisher = new MemoryKLineDataPublisher(streamClient, normalClient, 100, 200, 3)
                    .addListenKLine(btcusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.d_1, KLineInterval.m_1))
                    .addListenKLine(ethusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.d_1, KLineInterval.m_1));

            memoryKLineSource_btc = new MemoryKLineSource(btcusdt, KLineInterval.M_1, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
            memoryKLineSource_eth = new MemoryKLineSource(ethusdt, KLineInterval.M_1, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);

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

    @Test
    public void testIndicator() throws Exception {
        String macdName = "MACD-12-26-9";
        String rsiName = "RSI";
        new Thread(()->{
            try {
                new AutoTradeTask(env, memoryKLineSource_btc)
                        .addIndicator(new MACDCalculator(macdName, 12, 26, 9))
                        .addIndicator(new RSICalculator(rsiName, 15))
    //                .addSignalMaker(new MACDSignal_V1(macdName))
                        .execute("btc");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
        new Thread(()->{
            try {
                new AutoTradeTask(env2, memoryKLineSource_eth)
                        .addIndicator(new MACDCalculator(macdName, 12, 26, 9))
                        .addIndicator(new RSICalculator(rsiName, 15))
                        .addIndicator(new PSTCalculator("PST", 60, 3,3))
                        .execute("eth");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
        TimeUnit.SECONDS.sleep(1000);
    }

    @SneakyThrows
    @Test
    public void testPST() {
        new AutoTradeTask(env, memoryKLineSource_btc)
                .addIndicator(new PSTCalculator("PST", 60, 3,3))
                .execute("btc");
    }

    @Test
    public void testHistoryKLineLoader() throws InterruptedException {

        KLineBuffer kb = new KLineBuffer(10);

//        ArrayBlockingQueue<KLine> abq = new ArrayBlockingQueue<>(10);
        AtomicInteger counter = new AtomicInteger();

        new HistoryKLineLoader(200, normalClient, Executors.newFixedThreadPool(2))
                .startLoad("btcusdt", KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), kLines -> {
                    System.out.println("get klines count " + kLines.size());
                    for (KLine kLine : kLines) {
                        try {
                            kb.put(kLine);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("add kline " + counter.incrementAndGet() + ", buffer size " + kb.size());
                    }
                }).thenRun(()->{
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
