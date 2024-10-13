package com.helei.tradedatacenter.support;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.constants.WebSocketUrl;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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

    }
}
