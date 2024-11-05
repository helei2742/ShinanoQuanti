package com.helei.tradesignalprocess.support;


import com.helei.constants.trade.KLineInterval;
import com.helei.constants.trade.TradeSide;
import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.indicator.config.MACDConfig;
import com.helei.tradesignalprocess.config.FlinkConfig;
import com.helei.tradesignalprocess.stream.*;
import com.helei.tradesignalprocess.stream.a_klinesource.RandomKLineSource;
import com.helei.dto.trade.TradeSignal;
import com.helei.dto.trade.KLine;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.tradesignalprocess.stream.b_indicator.calculater.MACDCalculator;
import com.helei.tradesignalprocess.stream.b_indicator.calculater.PSTCalculator;
import com.helei.tradesignalprocess.stream.c_indicator_signal.IndicatorSignalService;
import com.helei.tradesignalprocess.stream.c_indicator_signal.IndicatorSignalStreamProcessor;
import com.helei.tradesignalprocess.stream.c_indicator_signal.maker.PSTSignalMaker;
import com.helei.tradesignalprocess.stream.d_decision.AbstractDecisionMaker;
import com.helei.tradesignalprocess.stream.b_indicator.calculater.BollCalculator;
import com.helei.dto.indicator.config.BollConfig;
import com.helei.dto.indicator.config.PSTConfig;
import com.helei.tradesignalprocess.stream.c_indicator_signal.maker.AbstractSignalMaker;
import com.helei.tradesignalprocess.stream.c_indicator_signal.maker.BollSignalMaker;
import com.helei.tradesignalprocess.stream.e_trade_signal.KafkaTradeSignalCommitter;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RandomKLineSourceTest {
    private static final Logger log = LoggerFactory.getLogger(RandomKLineSourceTest.class);
    private static String btcusdt = "btcusdt";

    private static String ethusdt = "ethusdt";

    private static StreamExecutionEnvironment env;

    private static StreamExecutionEnvironment env2;


    private static RandomKLineSource randomKLineSource;


    @BeforeAll
    public static void before() {
        try {
            env = FlinkConfig.streamExecutionEnvironment();
            randomKLineSource = new RandomKLineSource(btcusdt, Set.of(KLineInterval.m_15),
                    LocalDateTime.of(2020, 10, 29, 15, 38), 2000.0, 19000.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRandomKLineSource() throws Exception {
        DataStreamSource<KLine> streamSource = env.addSource(randomKLineSource);

        streamSource.print();

        env.execute();
        TimeUnit.MINUTES.sleep(1000);
    }

    @Test
    public void testAutoTradeV2() throws Exception {
        PSTConfig pstConfig = new PSTConfig(60, 3, 3);
        BollConfig bollConfig = new BollConfig(15);


        IndicatorSignalService indicatorSignalService = buildTradeSignalService(pstConfig, bollConfig);
        AbstractDecisionMaker<TradeSignal> abstractDecisionMaker = new AbstractDecisionMaker<>("测试用决策生成器") {
            @Serial
            private final static long serialVersionUID = 122142132145213L;

            @Override
            protected TradeSignal decisionAndBuilderOrder(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap) {
//                log.info("收到信号【{}】\n{}", symbol, windowSignal);
                return TradeSignal
                        .builder()
                        .symbol(symbol)
                        .tradeSide(TradeSide.BUY)
                        .targetPrice(BigDecimal.valueOf(windowSignal.getFirst().getTargetPrice()))
                        .stopPrice(BigDecimal.valueOf(windowSignal.getFirst().getStopPrice()))
                        .build();
            }
        };

        KafkaTradeSignalCommitter kafkaOriginOrderCommitter = new KafkaTradeSignalCommitter();


        TradeSignalBuildTask<TradeSignal> tradeSignalBuildTask = new TradeSignalBuildTask<TradeSignal>(
                indicatorSignalService,
                abstractDecisionMaker,
                kafkaOriginOrderCommitter);

        tradeSignalBuildTask.execute("test");
    }

    private IndicatorSignalService buildTradeSignalService(PSTConfig pstConfig, BollConfig bollConfig) {
        return IndicatorSignalService
                .builder(env)
                .addIndicatorSignalProcessor(
                        IndicatorSignalStreamProcessor
                                .builder()
                                .setWindowLengthRationOfKLine(1.0 / 60)
                                .addKLineSource(randomKLineSource)
                                .addIndicator(new PSTCalculator(pstConfig))
                                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
                                .addIndicator(new BollCalculator(bollConfig))

                                .addSignalMaker(new BollSignalMaker(bollConfig))
                                .addSignalMaker(new PSTSignalMaker(pstConfig))
                                .addSignalMaker(new AbstractSignalMaker(true) {

                                    @Override
                                    public void onOpen(OpenContext openContext) throws Exception {

                                    }

                                    @Override
                                    protected IndicatorSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
//                        System.out.println(Instant.ofEpochMilli(kLine.getOpenTime()) + " - " + kLine.getIndicators());
                                        return IndicatorSignal.builder().description("这是一条测试信号1h").name("测试信号1h")
                                                .kLine(kLine).tradeSide(TradeSide.BUY).targetPrice(1111111111.0).stopPrice(1231231.0).build();
                                    }

                                    @Override
                                    protected IndicatorSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
                                        return null;
                                    }
                                })
                                .build()
                )
                .build();
    }

}
