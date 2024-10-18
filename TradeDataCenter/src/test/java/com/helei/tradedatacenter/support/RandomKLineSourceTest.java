package com.helei.tradedatacenter.support;


import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.cexapi.binanceapi.constants.order.TradeSide;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.tradedatacenter.DecisionMakerService;
import com.helei.tradedatacenter.OrderCommitService;
import com.helei.tradedatacenter.TradeSignalService;
import com.helei.tradedatacenter.datasource.RandomKLineSource;
import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.decision.AbstractDecisionMaker;
import com.helei.tradedatacenter.resolvestream.indicator.Indicator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.BollCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.MACDCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.PSTCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.config.BollConfig;
import com.helei.tradedatacenter.resolvestream.indicator.config.IndicatorConfig;
import com.helei.tradedatacenter.resolvestream.indicator.config.MACDConfig;
import com.helei.tradedatacenter.resolvestream.indicator.config.PSTConfig;
import com.helei.tradedatacenter.resolvestream.signal.AbstractSignalMaker;
import com.helei.tradedatacenter.resolvestream.signal.BollSignalMaker;
import com.helei.tradedatacenter.resolvestream.signal.PSTSignalMaker;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class RandomKLineSourceTest {
    private static final Logger log = LoggerFactory.getLogger(RandomKLineSourceTest.class);
    private static String btcusdt = "btcusdt";

    private static String ethusdt = "ethusdt";

    private static RandomKLineSource btc_1h_source;
    private static RandomKLineSource btc_15m_source;


    @Autowired
    @Qualifier("flinkEnv")
    private StreamExecutionEnvironment env;

    @Autowired
    @Qualifier("flinkEnv2")
    private StreamExecutionEnvironment env2;


    @BeforeAll
    public static void before() {
        try {
            btc_1h_source = new RandomKLineSource(btcusdt, KLineInterval.h_1, LocalDateTime.of(2022, 10, 3, 0, 0), 2000.0, 19000.0);
            btc_15m_source = new RandomKLineSource(btcusdt, KLineInterval.m_15, LocalDateTime.of(2022, 10, 3, 0, 0), 2000.0, 19000.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAutoTradeV2() throws Exception {
        PSTConfig pstConfig = new PSTConfig(60, 3, 3);
        BollConfig bollConfig = new BollConfig(15);

        btc_1h_source.setRealTime(true);
        btc_15m_source.setRealTime(true);

        TradeSignalService tradeSignalService = buildTradeSignalService(pstConfig, bollConfig);
        DecisionMakerService decisionMakerService = new DecisionMakerService(new AbstractDecisionMaker("测试用决策生成器") {
            @Override
            protected OriginOrder decisionAndBuilderOrder(String symbol, List<TradeSignal> windowSignal, HashMap<IndicatorConfig<? extends Indicator>, Indicator> indicatorMap) {
                log.info("收到信号【{}】\n{}", symbol, windowSignal);
                return OriginOrder
                        .builder()
                        .symbol(symbol)
                        .tradeSide(TradeSide.BUY)
//                        .targetPrice(BigDecimal.valueOf(windowSignal.getFirst().getTargetPrice()))
//                        .stopPrice(BigDecimal.valueOf(windowSignal.getFirst().getStopPrice()))
                        .build();
            }
        });

        OrderCommitService orderCommitService = new OrderCommitService();


        AutoTradeTask autoTradeTask = new AutoTradeTask(tradeSignalService, decisionMakerService, orderCommitService);
        autoTradeTask.execute("test");
    }

    private TradeSignalService buildTradeSignalService(PSTConfig pstConfig, BollConfig bollConfig) {
        return TradeSignalService
                .builder(env)
                .buildResolver()
                .setWindowLengthRationOfKLine(1.0 / 60)
                .addKLineSource(btc_1h_source)
                .addIndicator(new PSTCalculator(pstConfig))
                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
                .addIndicator(new BollCalculator(bollConfig))
                .addSignalMaker(new BollSignalMaker(bollConfig))
                .addSignalMaker(new PSTSignalMaker(pstConfig))
                .addSignalMaker(new AbstractSignalMaker(true) {
                    private Random random = new Random();

                    @Override
                    public void onOpen(OpenContext openContext) throws Exception {

                    }

                    @Override
                    protected TradeSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
                        if (random.nextBoolean()) {
                            return null;
                        }
                        return TradeSignal.builder().description("这是一条测试信号1h").name("测试信号1h").tradeSide(TradeSide.BUY).build();
                    }

                    @Override
                    protected TradeSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
                        if (random.nextBoolean()) {
                            return null;
                        }

                        return TradeSignal.builder().description("这是一条测试信号1h").name("测试信号1h").tradeSide(TradeSide.BUY).build();
                    }
                })
                .addInService()

                .buildResolver()
                .setWindowLengthRationOfKLine(1.0 / 15)
                .addKLineSource(btc_15m_source)
                .addIndicator(new PSTCalculator(pstConfig))
                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
                .addIndicator(new BollCalculator(bollConfig))
                .addSignalMaker(new BollSignalMaker(bollConfig))
                .addSignalMaker(new PSTSignalMaker(pstConfig))
                .addSignalMaker(new AbstractSignalMaker(true) {
                    private Random random = new Random();

                    @Override
                    public void onOpen(OpenContext openContext) throws Exception {

                    }

                    @Override
                    protected TradeSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
                        if (random.nextBoolean()) {
                            return null;
                        }
                        return TradeSignal.builder().description("这是一条测试信号").name("测试信号15m").tradeSide(TradeSide.BUY).build();
                    }

                    @Override
                    protected TradeSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
                        if (random.nextBoolean()) {
                            return null;
                        }

                        return TradeSignal.builder().description("这是一条测试信号").name("测试信号15m").tradeSide(TradeSide.BUY).build();
                    }
                })
                .addInService()
                .build();
    }

}