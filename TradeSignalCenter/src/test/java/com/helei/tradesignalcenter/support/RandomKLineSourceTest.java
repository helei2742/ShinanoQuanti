package com.helei.tradesignalcenter.support;


import com.helei.cexapi.CEXApiFactory;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.constants.KLineInterval;
import com.helei.constants.TradeSide;
import com.helei.dto.ASKey;
import com.helei.binanceapi.constants.BinanceApiUrl;
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
    private static RandomKLineSource btc_1m_source;

    private static BinanceWSApiClient normalClient;

    @Autowired
    @Qualifier("flinkEnv")
    private StreamExecutionEnvironment env;

    @Autowired
    @Qualifier("flinkEnv2")
    private StreamExecutionEnvironment env2;




    @BeforeAll
    public static void before() {
        try {
            normalClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);

            normalClient.connect().get();

            btc_1h_source = new RandomKLineSource(btcusdt, KLineInterval.h_1, LocalDateTime.of(2022, 10, 3, 0, 0), 2000.0, 19000.0);
            btc_15m_source = new RandomKLineSource(btcusdt, KLineInterval.m_15, LocalDateTime.of(2022, 10, 3, 0, 0), 2000.0, 19000.0);
            btc_1m_source = new RandomKLineSource(btcusdt, KLineInterval.m_1, LocalDateTime.of(2022, 10, 3, 0, 0), 2000.0, 19000.0);

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
        btc_1m_source.setRealTime(true);

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

        AccountInfoService accountInfoService = new AccountInfoService(normalClient);
        String testId = "testId";
        accountInfoService.getUid2UserInfo().put(testId, new UserInfo(testId, new ASKey(ak, sk), List.of("BTCUSDT"), new AccountLocationConfig(0.2, 10 , 50)));
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
                .addKLineSource(btc_1m_source)
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
