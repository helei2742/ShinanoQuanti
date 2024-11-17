package com.helei.tradesignalprocess;

import com.helei.constants.trade.KLineInterval;
import com.helei.dto.indicator.config.BollConfig;
import com.helei.dto.indicator.config.MACDConfig;
import com.helei.dto.trade.*;
import com.helei.tradesignalprocess.config.FlinkConfig;
import com.helei.tradesignalprocess.stream.TradeSignalBuildTask;
import com.helei.tradesignalprocess.stream.a_klinesource.impl.BinanceKLineHisAndRTSource;
import com.helei.tradesignalprocess.stream.b_indicator.calculater.BollCalculator;
import com.helei.tradesignalprocess.stream.b_indicator.calculater.MACDCalculator;
import com.helei.tradesignalprocess.stream.c_indicator_signal.IndicatorSignalService;
import com.helei.tradesignalprocess.stream.c_indicator_signal.IndicatorSignalStreamProcessor;
import com.helei.tradesignalprocess.stream.c_indicator_signal.maker.BollSignalMaker;
import com.helei.tradesignalprocess.stream.c_indicator_signal.maker.MACDSignal_V1;
import com.helei.tradesignalprocess.stream.d_trade_signal.impl.IndicatorAllMapDecisionMaker;
import com.helei.tradesignalprocess.stream.e_sink.KafkaTradeSignalCommitter;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

public class TradeSignalProcess {
    private static String symbol = "btcusdt";

    private static StreamExecutionEnvironment env;

    public static void main(String[] args) throws Exception {
        env = FlinkConfig.streamExecutionEnvironment();

        BinanceKLineHisAndRTSource source = new BinanceKLineHisAndRTSource(
                symbol,
                Set.of(KLineInterval.m_15),
                LocalDateTime.of(2024, 11, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        );

        MACDConfig macdConfig = new MACDConfig(12, 26, 9);
        BollConfig bollConfig = new BollConfig(15);

        /*
         * 产生指标信号
         */
        IndicatorSignalService indicatorSignalService = IndicatorSignalService
                .builder(env)
                .addIndicatorSignalProcessor(
                        IndicatorSignalStreamProcessor
                                .builder()
                                .setWindowLengthRationOfKLine(1.0 / 15)
                                .addKLineSource(source)
                                .addIndicator(new BollCalculator(bollConfig))
                                .addIndicator(new MACDCalculator(macdConfig))
                                .addSignalMaker(new BollSignalMaker(bollConfig))
                                .addSignalMaker(new MACDSignal_V1(macdConfig))
                                .build()
                )
                .build();

        KafkaTradeSignalCommitter kafkaOriginOrderCommitter = new KafkaTradeSignalCommitter();

        TradeSignalBuildTask<TradeSignal> tradeSignalBuildTask =
                new TradeSignalBuildTask<>(
                        indicatorSignalService,
                        new IndicatorAllMapDecisionMaker(),
                        kafkaOriginOrderCommitter
                );

        tradeSignalBuildTask.execute("test");
        env.execute();
    }
}
