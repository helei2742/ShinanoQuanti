package com.helei.tradedatacenter.support;


import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.tradedatacenter.constants.KLineInterval;
import com.helei.tradedatacenter.datasource.MemoryKLineDataPublisher;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.indicator.calculater.MACDCalculator;
import com.helei.tradedatacenter.indicator.calculater.RSICalculator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KLineTradingDecision {
    private static BinanceWSApiClientClient binanceWSApiClient = null;

    @BeforeAll
    public static void before() {
        try {
            binanceWSApiClient = CEXApiFactory.binanceApiClient(5, WebSocketUrl.WS_STREAM_URL);
            binanceWSApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    @Qualifier("flinkEnv")
    private StreamExecutionEnvironment env;


    @Test
    public void testIndicator() throws Exception {
        MemoryKLineDataPublisher dataPublisher =
                new MemoryKLineDataPublisher(4, WebSocketUrl.WS_STREAM_URL)
                        .addListenKLine("btcusdt", Arrays.asList(KLineInterval.d_1, KLineInterval.h_1, KLineInterval.m_15));

        MemoryKLineSource memoryKLineSource =
                new MemoryKLineSource("btcusdt", KLineInterval.m_15,dataPublisher);

        new AutoTradeTask(env, memoryKLineSource)
                .addIndicator(new MACDCalculator("MACD-12-26-9", 12, 26, 9))
                .addIndicator(new RSICalculator("RSI", 15))
                .execute();

    }
}
