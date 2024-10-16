package com.helei.tradedatacenter.support;


import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.cexapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.TradeSignalService;
import com.helei.tradedatacenter.datasource.HistoryKLineLoader;
import com.helei.tradedatacenter.datasource.MemoryKLineDataPublisher;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.resolvestream.GroupSignalResolver;
import com.helei.tradedatacenter.resolvestream.decision.AbstractDecisionMaker;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.decision.PSTBollDecisionMaker;
import com.helei.tradedatacenter.resolvestream.decision.config.PSTBollDecisionConfig_v1;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.BollCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.MACDCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.PSTCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.RSICalculator;
import com.helei.tradedatacenter.resolvestream.indicator.config.BollConfig;
import com.helei.tradedatacenter.resolvestream.indicator.config.MACDConfig;
import com.helei.tradedatacenter.resolvestream.indicator.config.PSTConfig;
import com.helei.tradedatacenter.resolvestream.indicator.config.RSIConfig;
import com.helei.tradedatacenter.resolvestream.order.AbstractOrderCommitter;
import com.helei.tradedatacenter.resolvestream.signal.BollSignalMaker;
import com.helei.tradedatacenter.resolvestream.signal.PSTSignalMaker;
import com.helei.tradedatacenter.util.KLineBuffer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
            streamClient = CEXApiFactory.binanceApiClient(4, WebSocketUrl.WS_STREAM_URL);
            normalClient = CEXApiFactory.binanceApiClient(4, WebSocketUrl.WS_NORMAL_URL);

            CompletableFuture.allOf(streamClient.connect(), normalClient.connect()).get();

            dataPublisher = new MemoryKLineDataPublisher(streamClient, normalClient, 100, 200, 3)
                    .addListenKLine(btcusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.h_2, KLineInterval.m_15))
                    .addListenKLine(ethusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.d_1, KLineInterval.m_15));

            memoryKLineSource_btc_2h = new MemoryKLineSource(btcusdt, KLineInterval.h_2, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
            memoryKLineSource_btc_15m = new MemoryKLineSource(btcusdt, KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);

            memoryKLineSource_eth = new MemoryKLineSource(ethusdt, KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
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
//        new Thread(()->{
//            try {
//                new AutoTradeTask(env, memoryKLineSource_btc)
//                        .addIndicator(new MACDCalculator(macdName, 12, 26, 9))
//                        .addIndicator(new RSICalculator(rsiName, 15))
//                        .addSignalMaker(new AbstractSignalMaker(true) {
//                            @Override
//                            public void onOpen(OpenContext openContext) throws Exception {
//
//                            }
//
//                            @Override
//                            protected void stateUpdate(KLine kLine) throws IOException {
//                                System.out.println(kLine);
//                            }
//
//                            @Override
//                            protected TradeSignal buildSignal(KLine kLine) throws IOException {
//                                return null;
//                            }
//                        })
//                        .addDecisionMaker(new AbstractDecisionMaker() {
//                            @Override
//                            public BaseOrder decisionAndBuilderOrder(TradeSignal signal) {
//
//                                return null;
//                            }
//                        })
//                        .addOrderCommiter(new AbstractOrderCommitter() {
//                            @Override
//                            public boolean commitTradeOrder(BaseOrder order) {
//                                return false;
//                            }
//                        })
//                        .execute("btc");
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//        }).start();
//        new Thread(()->{
//            try {
//                PSTConfig pstConfig = new PSTConfig(60, 3, 3);
//                new AutoTradeTask(env2, memoryKLineSource_eth)
////                        .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
////                        .addIndicator(new RSICalculator(new RSIConfig(15)))
//                        .addIndicator(new PSTCalculator(pstConfig))
//                        .addSignalMaker(new PSTSignalMaker(pstConfig))
//                        .addDecisionMaker(new AbstractDecisionMaker() {
//                            @Override
//                            public BaseOrder decisionAndBuilderOrder(TradeSignal signal) {
//                                System.out.println(signal);
//                                return null;
//                            }
//                        })
//                        .addOrderCommiter(new AbstractOrderCommitter() {
//                            @Override
//                            public boolean commitTradeOrder(BaseOrder order) {
//                                return false;
//                            }
//                        })
//                        .execute("eth");
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//        }).start();
//        TimeUnit.SECONDS.sleep(1000);
    }

    @Test
    public void testAutoTradeV2() throws Exception {
        PSTConfig pstConfig = new PSTConfig(60, 3, 3);

        BollConfig bollConfig = new BollConfig(15);
        TradeSignalService tradeSignalService = TradeSignalService
                .builder(env)
//                .buildResolver()
//                .addKLineSource(memoryKLineSource_btc_15m)
//                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
//                .addIndicator(new RSICalculator(new RSIConfig(15)))
//                .addIndicator(new PSTCalculator(pstConfig))
//                .addSignalMaker(new PSTSignalMaker(pstConfig))
//                .addInService()
                .buildResolver()
                .addKLineSource(memoryKLineSource_btc_2h)
                .addIndicator(new PSTCalculator(pstConfig))
                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
                .addIndicator(new BollCalculator(bollConfig))
                .addSignalMaker(new BollSignalMaker(bollConfig))
                .addSignalMaker(new PSTSignalMaker(pstConfig))
                .addGroupSignalResolver(new GroupSignalResolver() {
                    private transient BufferedWriter writer;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        writer = new BufferedWriter(new FileWriter("test-kline-file.txt", true));
                    }

                    @Override
                    public void invoke(Tuple2<KLine, List<TradeSignal>> value, Context context) throws Exception {
                        List<TradeSignal> list = value.getField(1);

                        if (list.isEmpty()) return;

                        writer.write("\n<<start>>\n");
                        writer.write(value.getField(0).toString());
                        writer.newLine();

                        for (TradeSignal tradeSignal : list) {
                            writer.write(tradeSignal.toString());
                        }
                        writer.write("<<end>>\n");
                    }

                    // 任务结束时调用，关闭文件流
                    @Override
                    public void close() throws Exception {
                        if (writer != null) {
                            writer.flush();
                            writer.close(); // 关闭文件流
                        }
                        super.close();
                    }
                })
//                .addSignalMaker(new PSTSignalMaker(pstConfig))
                .addInService()
                .build();

        AutoTradeTask autoTradeTask = new AutoTradeTask(tradeSignalService);

        autoTradeTask
                .addDecisionMaker(new PSTBollDecisionMaker(new PSTBollDecisionConfig_v1(pstConfig, bollConfig)))
                .addOrderCommiter(new AbstractOrderCommitter() {
                    @Override
                    public boolean commitTradeOrder(OriginOrder order) {
                        System.out.println(order);
                        return false;
                    }
                })
                .execute("test");


    }

    @SneakyThrows
    @Test
    public void testPST() {

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