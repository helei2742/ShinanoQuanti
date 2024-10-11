package com.helei.tradedatacenter.support;


import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamParamKey;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamType;
import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;
import com.helei.cexapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.AutoTradeTask;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.indicator.calculater.BollCalculator;
import com.helei.tradedatacenter.indicator.calculater.EMACalculator;
import com.helei.tradedatacenter.indicator.calculater.RSICalculator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

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
        MemoryKLineSource memoryKLineSource = new MemoryKLineSource();

        binanceWSApiClient
                .getStreamApi()
                .builder()
                .symbol("btcudt")
                .addSubscribeEntity(
                        StreamSubscribeEntity
                                .builder()
                                .symbol("btcusdt")
                                .subscribeType(WebSocketStreamType.KLINE)
                                .invocationHandler((streamName, result) -> {
                                    System.out.println("<<<<<<======================");
                                    System.out.println(streamName);
//                                    System.out.println(result);
                                    try {

                                        memoryKLineSource.append(result.getJSONObject("data").getJSONObject("k"));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("======================>>>>>>");
                                })
                                .build()

                                .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, "1m")
                                .addParam(WebSocketStreamParamKey.SECRET_KEY, "123")
                                .addParam(WebSocketStreamParamKey.API_KEY, "123")
                                .isSignature(false)
                )
                .subscribe();


        new AutoTradeTask(env, memoryKLineSource)
                .addIndicator(new BollCalculator(15))
                .addIndicator(new RSICalculator(15))
                .addIndicator(new EMACalculator(5))
                .addIndicator(new EMACalculator(20))
                .addIndicator(new EMACalculator(60))
                .addIndicator(new EMACalculator(120))
                .execute();

        TimeUnit.SECONDS.sleep(10000);
    }
}
