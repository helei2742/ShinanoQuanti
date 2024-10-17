package com.helei.tradedatacenter.support;


        import com.helei.cexapi.CEXApiFactory;
        import com.helei.cexapi.binanceapi.BinanceWSApiClient;
        import com.helei.cexapi.binanceapi.constants.KLineInterval;
        import com.helei.cexapi.binanceapi.constants.order.TradeSide;
        import com.helei.cexapi.constants.WebSocketUrl;
        import com.helei.tradedatacenter.AutoTradeTask;
        import com.helei.tradedatacenter.TradeSignalService;
        import com.helei.tradedatacenter.datasource.MemoryKLineDataPublisher;
        import com.helei.tradedatacenter.datasource.MemoryKLineSource;
        import com.helei.tradedatacenter.datasource.RandomKLineSource;
        import com.helei.tradedatacenter.dto.OriginOrder;
        import com.helei.tradedatacenter.entity.KLine;
        import com.helei.tradedatacenter.entity.TradeSignal;
        import com.helei.tradedatacenter.resolvestream.GroupSignalResolver;
        import com.helei.tradedatacenter.resolvestream.decision.PSTBollDecisionMaker;
        import com.helei.tradedatacenter.resolvestream.decision.config.PSTBollDecisionConfig_v1;
        import com.helei.tradedatacenter.resolvestream.indicator.calculater.BollCalculator;
        import com.helei.tradedatacenter.resolvestream.indicator.calculater.MACDCalculator;
        import com.helei.tradedatacenter.resolvestream.indicator.calculater.PSTCalculator;
        import com.helei.tradedatacenter.resolvestream.indicator.config.BollConfig;
        import com.helei.tradedatacenter.resolvestream.indicator.config.MACDConfig;
        import com.helei.tradedatacenter.resolvestream.indicator.config.PSTConfig;
        import com.helei.tradedatacenter.resolvestream.order.AbstractOrderCommitter;
        import com.helei.tradedatacenter.resolvestream.signal.AbstractSignalMaker;
        import com.helei.tradedatacenter.resolvestream.signal.BollSignalMaker;
        import com.helei.tradedatacenter.resolvestream.signal.PSTSignalMaker;
        import com.helei.tradedatacenter.signal.TestSignalMaker;
        import org.apache.flink.api.common.functions.OpenContext;
        import org.apache.flink.api.java.tuple.Tuple2;
        import org.apache.flink.configuration.Configuration;
        import org.apache.flink.streaming.api.TimerService;
        import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
        import org.junit.jupiter.api.BeforeAll;
        import org.junit.jupiter.api.Test;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.beans.factory.annotation.Qualifier;
        import org.springframework.boot.test.context.SpringBootTest;

        import java.io.BufferedWriter;
        import java.io.FileWriter;
        import java.time.LocalDateTime;
        import java.util.Arrays;
        import java.util.List;
        import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class RandomKLineSourceTest {
    private static String btcusdt = "btcusdt";

    private static String ethusdt = "ethusdt";

    private static RandomKLineSource btc_1h_source;


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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAutoTradeV2() throws Exception {
        PSTConfig pstConfig = new PSTConfig(60, 3, 3);
        BollConfig bollConfig = new BollConfig(15);

        btc_1h_source.setRealTime(true);

        TradeSignalService tradeSignalService = TradeSignalService
                .builder(env)
                .buildResolver()
                .setWindowLengthRationOfKLine(1.0/60)
                .setAllowSignalDelay(10000)
                .addKLineSource(btc_1h_source)
                .addIndicator(new PSTCalculator(pstConfig))
                .addIndicator(new MACDCalculator(new MACDConfig(12, 26, 9)))
                .addIndicator(new BollCalculator(bollConfig))
                .addSignalMaker(new BollSignalMaker(bollConfig))
                .addSignalMaker(new PSTSignalMaker(pstConfig))
                .addSignalMaker(new TestSignalMaker())
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
                        writer.write("\n<<end>>\n");
                        writer.flush();
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

}
