
package com.helei.tradesignalcenter.support;


import com.helei.constants.KLineInterval;
import com.helei.constants.TradeSide;
import com.helei.dto.ASKey;
import com.helei.tradesignalcenter.config.FlinkConfig;
import com.helei.tradesignalcenter.resolvestream.*;
import com.helei.tradesignalcenter.resolvestream.a_datasource.RandomKLineSource;
import com.helei.dto.account.AccountLocationConfig;
import com.helei.tradesignalcenter.dto.OriginOrder;
import com.helei.dto.account.UserInfo;
import com.helei.dto.KLine;
import com.helei.dto.TradeSignal;
import com.helei.tradesignalcenter.resolvestream.c_signal.TradeSignalService;
import com.helei.tradesignalcenter.resolvestream.d_decision.DecisionMakerService;
import com.helei.tradesignalcenter.resolvestream.d_decision.maker.AbstractDecisionMaker;
import com.helei.dto.indicator.Indicator;
import com.helei.tradesignalcenter.resolvestream.b_indicator.calculater.BollCalculator;
import com.helei.tradesignalcenter.resolvestream.b_indicator.calculater.MACDCalculator;
import com.helei.tradesignalcenter.resolvestream.b_indicator.calculater.PSTCalculator;
import com.helei.dto.indicator.config.BollConfig;
import com.helei.dto.indicator.config.IndicatorConfig;
import com.helei.dto.indicator.config.MACDConfig;
import com.helei.dto.indicator.config.PSTConfig;
import com.helei.tradesignalcenter.resolvestream.c_signal.maker.AbstractSignalMaker;
import com.helei.tradesignalcenter.resolvestream.c_signal.maker.BollSignalMaker;
import com.helei.tradesignalcenter.resolvestream.c_signal.maker.PSTSignalMaker;
import com.helei.tradesignalcenter.resolvestream.e_order.OrderCommitService;
import com.helei.tradesignalcenter.service.AccountInfoService;
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
import java.util.Random;
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

        String sk = "C1ihCOkWEECpnsbx4HcFLZubOyZX2CvPVaIvxlHtDNwfai8WsEzIxV6rLIizvgl9";
        String ak = "HZzsqyA0uBTC4GzaykzeUL5ml7V0jzGXGwU38WGDUmH8JLzPIw3ZfbGxa4ZzuzFm";

        AccountInfoService accountInfoService = new AccountInfoService();
        String testId = "testId";
        accountInfoService.getUid2UserInfo().put(testId, new UserInfo(testId, new ASKey(ak, sk), List.of("BTCUSDT"), new AccountLocationConfig(0.2, 10, 50)));
        accountInfoService.getSymbol2UIdsMap().put("BTCUSDT", List.of(testId));
        OrderCommitService orderCommitService = new OrderCommitService(accountInfoService, new LimitOrderBuildSupporter());


        AutoTradeTask autoTradeTask = new AutoTradeTask(tradeSignalService, decisionMakerService, orderCommitService);
        autoTradeTask.execute("test");
    }

    private TradeSignalService buildTradeSignalService(PSTConfig pstConfig, BollConfig bollConfig) {
        return TradeSignalService
                .builder(env)
                .buildResolver()
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

