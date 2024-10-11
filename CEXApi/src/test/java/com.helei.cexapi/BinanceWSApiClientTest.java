

package com.helei.cexapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamParamKey;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamType;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;
import com.helei.cexapi.constants.WebSocketUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;


class BinanceWSApiClientTest {
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

    @Test
    public void testAGG_TRADE() throws InterruptedException {
        binanceWSApiClient
                .setSignature(new ASKey())
                .getStreamApi()
                .builder()
                .symbol("btcusdt")

                .addSubscribeEntity(
                        WebSocketStreamType.AGG_TRADE,
                        (streamName, result) -> {
                            System.out.println(streamName);
                        }
                )
                .subscribe();

        TimeUnit.SECONDS.sleep(100);
    }

    @Test
    public void testKLine() throws InterruptedException {
        binanceWSApiClient
                .setSignature(new ASKey())
                .getStreamApi()
                .builder()
                .symbol("btcusdt")
                .addSubscribeEntity(
                        StreamSubscribeEntity
                                .builder()
                                .symbol("btcusdt")
                                .subscribeType(WebSocketStreamType.KLINE)
                                .invocationHandler((streamName, result) -> {
                                    System.out.println("<<<<<<======================");
                                    System.out.println(streamName);
                                    System.out.println(result);
                                    System.out.println("======================>>>>>>");
                                })
                                .build()

                                .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, "1m")
                                .addParam(WebSocketStreamParamKey.SECRET_KEY, "123")
                                .addParam(WebSocketStreamParamKey.API_KEY, "123")
                                .isSignature(false)
                )
                .subscribe();

        TimeUnit.SECONDS.sleep(100);
    }
}