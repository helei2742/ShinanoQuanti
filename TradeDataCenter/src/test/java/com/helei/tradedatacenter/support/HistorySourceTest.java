package com.helei.tradedatacenter.support;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.cexapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.tradedatacenter.datasource.HistoryKLineSource;
import com.helei.tradedatacenter.dto.SubscribeData;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.calculater.PSTCalculator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class HistorySourceTest {
    private static BinanceWSApiClient binanceWSApiClient = null;

    @Autowired
    @Qualifier("flinkEnv")
    private StreamExecutionEnvironment env;

    @BeforeAll
    public static void before() {
        try {
            binanceWSApiClient = CEXApiFactory.binanceApiClient(5, WebSocketUrl.WS_NORMAL_URL);
            binanceWSApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testHistorySource() throws Exception {
        HistoryKLineSource historyKLineSource = new HistoryKLineSource("BTCUSDT",
                KLineInterval.d_1,
                LocalDateTime.of(2019, 1, 1, 0, 0),
                10,
                binanceWSApiClient
        );

        SubscribeData<List<KLine>> subscribeData = historyKLineSource.startLoadHistory();

        new Thread(()->{
            try {
                List<KLine> l = null;
                while ((l=subscribeData.getData()) != null && !l.isEmpty()) {
                    System.out.println(l);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

        TimeUnit.SECONDS.sleep(1000);
    }
}
