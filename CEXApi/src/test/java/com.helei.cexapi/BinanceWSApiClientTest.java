package com.helei.cexapi;

import com.helei.binanceapi.BinanceWSMarketStreamClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.RunEnv;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.constants.trade.TradeType;
import com.helei.dto.config.RunTypeConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class BinanceWSApiClientTest {

    private static RunEnv runEnv;

    private static TradeType tradeType;

    private static BinanceBaseClientManager clientManager = null;

    private static BinanceWSMarketStreamClient marketStreamClient;

    @BeforeAll
    public static void before() {
        try {
            clientManager = CEXApiFactory.binanceBaseWSClientManager(new RunTypeConfig(), Executors.newVirtualThreadPerTaskExecutor());

            marketStreamClient = (BinanceWSMarketStreamClient) clientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.MARKET_STREAM).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAGG_TRADE() throws InterruptedException {
        marketStreamClient
                .getStreamApi()
                .builder()
                .symbol("btcusdt")
                .addSubscribeEntity(
                        WebSocketStreamType.AGG_TRADE,
                        (streamName, params, result) -> {
                            System.out.println(streamName);
                        }
                )
                .subscribe();

        TimeUnit.SECONDS.sleep(100);
    }

    @Test
    public void testKLine() throws InterruptedException {
        marketStreamClient
                .getStreamApi()
                .builder()
                .symbol("btcusdt")
                .addSubscribeEntity(
                        StreamSubscribeEntity
                                .builder()
                                .symbol("btcusdt")
                                .subscribeType(WebSocketStreamType.KLINE)
                                .invocationHandler((streamName, params, result) -> {
                                    System.out.println("<<<<<<======================");
                                    System.out.println(streamName);
                                    System.out.println(result);
                                    System.out.println("======================>>>>>>");
                                })
                                .build()
                                .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, "1m")
                                .addParam(WebSocketStreamParamKey.SECRET_KEY, "123")
                                .addParam(WebSocketStreamParamKey.API_KEY, "123")
                )
                .subscribe();

        TimeUnit.SECONDS.sleep(100);
    }
}
