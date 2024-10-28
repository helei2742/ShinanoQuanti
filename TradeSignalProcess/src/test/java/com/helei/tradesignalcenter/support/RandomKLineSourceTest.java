
package com.helei.tradesignalcenter.support;


import com.helei.constants.KLineInterval;
import com.helei.constants.TradeSide;
import com.helei.dto.IndicatorMap;
import com.helei.tradesignalcenter.config.FlinkConfig;
import com.helei.tradesignalcenter.stream.*;
import com.helei.tradesignalcenter.stream.a_datasource.RandomKLineSource;
import com.helei.tradesignalcenter.dto.OriginOrder;
import com.helei.dto.KLine;
import com.helei.dto.TradeSignal;
import com.helei.tradesignalcenter.stream.c_signal.TradeSignalService;
import com.helei.tradesignalcenter.stream.d_decision.AbstractDecisionMaker;
import com.helei.dto.indicator.Indicator;
import com.helei.tradesignalcenter.stream.b_indicator.calculater.BollCalculator;
import com.helei.tradesignalcenter.stream.b_indicator.calculater.MACDCalculator;
//import com.helei.tradesignalcenter.stream.b_indicator.calculater.PSTCalculator;
import com.helei.dto.indicator.config.BollConfig;
import com.helei.dto.indicator.config.IndicatorConfig;
import com.helei.dto.indicator.config.MACDConfig;
import com.helei.dto.indicator.config.PSTConfig;
import com.helei.tradesignalcenter.stream.c_signal.maker.AbstractSignalMaker;
import com.helei.tradesignalcenter.stream.c_signal.maker.BollSignalMaker;
//import com.helei.tradesignalcenter.stream.c_signal.maker.PSTSignalMaker;
import com.helei.tradesignalcenter.stream.e_order.KafkaOriginOrderCommitter;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
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
            randomKLineSource = new RandomKLineSource(btcusdt, Set.of(KLineInterval.m_1),
                    LocalDateTime.of(2022, 10, 27, 22, 0), 2000.0, 19000.0);

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


        TradeSignalService tradeSignalService = buildTradeSignalService(pstConfig, bollConfig);
        AbstractDecisionMaker<OriginOrder> abstractDecisionMaker = new AbstractDecisionMaker<>("测试用决策生成器") {
            @Override
            protected OriginOrder decisionAndBuilderOrder(String symbol, List<TradeSignal> windowSignal, IndicatorMap indicatorMap) {
                log.info("收到信号【{}】\n{}", symbol, windowSignal);
                return OriginOrder
                        .builder()
                        .symbol(symbol)
                        .tradeSide(TradeSide.BUY)
//                        .targetPrice(BigDecimal.valueOf(windowSignal.getFirst().getTargetPrice()))
//                        .stopPrice(BigDecimal.valueOf(windowSignal.getFirst().getStopPrice()))
                        .build();
            }
        };

        AutoTradeTask<OriginOrder> autoTradeTask = new AutoTradeTask<OriginOrder>(
                tradeSignalService,
                abstractDecisionMaker,
                null);
        autoTradeTask.execute("test");
    }

    private TradeSignalService buildTradeSignalService(PSTConfig pstConfig, BollConfig bollConfig) {
        return TradeSignalService
                .builder(env)
                .buildResolver()
                .setWindowLengthRationOfKLine(1.0 / 60)
                .addKLineSource(randomKLineSource)
//                .addIndicator(new PSTCalculator(pstConfig))
                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
                .addIndicator(new MACDCalculator(new MACDConfig(12, 8, 9)))
//                .addIndicator(new BollCalculator(bollConfig))
                .addSignalMaker(new BollSignalMaker(bollConfig))
//                .addSignalMaker(new PSTSignalMaker(pstConfig))
                .addSignalMaker(new AbstractSignalMaker(true) {

                    @Override
                    public void onOpen(OpenContext openContext) throws Exception {

                    }

                    @Override
                    protected TradeSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
                        System.out.println(Instant.ofEpochMilli(kLine.getOpenTime()) + " - " + kLine.getIndicators());
                        return null;
                    }

                    @Override
                    protected TradeSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
                        return null;
                    }
                })
//                .addSignalMaker(new AbstractSignalMaker(true) {
//                    private Random random = new Random();
//
//                    @Override
//                    public void onOpen(OpenContext openContext) throws Exception {
//
//                    }
//
//                    @Override
//                    protected TradeSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
//                        if (random.nextBoolean()) {
//                            return null;
//                        }
//                        return TradeSignal.builder().description("这是一条测试信号1h").name("测试信号1h").tradeSide(TradeSide.BUY).build();
//                    }
//
//                    @Override
//                    protected TradeSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
//                        if (random.nextBoolean()) {
//                            return null;
//                        }
//
//                        return TradeSignal.builder().description("这是一条测试信号1h").name("测试信号1h").tradeSide(TradeSide.BUY).build();
//                    }
//                })
                .addInService()
                .build();
    }

}
