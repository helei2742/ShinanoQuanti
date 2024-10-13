package com.helei.tradedatacenter.support;


import com.helei.cexapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.datasource.MemoryKLineDataPublisher;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.indicator.calculater.MACDCalculator;
import com.helei.tradedatacenter.indicator.calculater.PSTCalculator;
import com.helei.tradedatacenter.indicator.calculater.RSICalculator;
import com.helei.tradedatacenter.signal.MACDSignal_V1;
import lombok.SneakyThrows;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KLineTradingDecision {
    private MemoryKLineDataPublisher dataPublisher;

    private MemoryKLineSource memoryKLineSource;

    private String btcusdt = "btcusdt";

    @BeforeAll
    public void before() {
        try {
            dataPublisher =
            new MemoryKLineDataPublisher(4, WebSocketUrl.WS_STREAM_URL)
//                        .addListenKLine("btcusdt", Arrays.asList(KLineInterval.d_1, KLineInterval.h_1, KLineInterval.m_15));
                    .addListenKLine(btcusdt, List.of(KLineInterval.m_1));

            memoryKLineSource = new MemoryKLineSource(btcusdt, KLineInterval.m_1,dataPublisher);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    @Qualifier("flinkEnv")
    private StreamExecutionEnvironment env;


    @Test
    public void testIndicator() throws Exception {
        String macdName = "MACD-12-26-9";
        String rsiName = "RSI";
        new AutoTradeTask(env, memoryKLineSource)
                .addIndicator(new MACDCalculator(macdName, 12, 26, 9))
                .addIndicator(new RSICalculator(rsiName, 15))
                .addSignalMaker(new MACDSignal_V1(macdName))
                .execute();

    }

    @SneakyThrows
    @Test
    public void testPST() {
        new AutoTradeTask(env, memoryKLineSource)
                .addIndicator(new PSTCalculator("PST", 60, 3,3))
                .execute();
    }

}
