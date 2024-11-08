package com.helei.binanceapi.api.rest;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.constants.api.BinanceRestApiType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.trade.KLine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


class BinanceRestHttpApiClientTest {
    private static BinanceRestHttpApiClient binanceRestHttpApiClient;
    @BeforeAll
    public static void setup() {
        binanceRestHttpApiClient = new BinanceRestHttpApiClient(BinanceApiConfig.INSTANCE, Executors.newVirtualThreadPerTaskExecutor());
    }

    @Test
    void queryBinanceApi() throws ExecutionException, InterruptedException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("symbol", "BTCUSDT");
        jsonObject.put("interval", "1d");
        CompletableFuture<List<KLine>> stringCompletableFuture = binanceRestHttpApiClient.queryBinanceApi(
                RunEnv.NORMAL,
                TradeType.SPOT,
                BinanceRestApiType.KLINE,
                jsonObject,
                null
        );

        List<KLine> kLines = stringCompletableFuture.get();

        System.out.println(kLines);

    }


    @Test
    public void testGetListenKey() throws InterruptedException, ExecutionException {

        ASKey asKey = new ASKey("b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66", "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f");
        ASKey asKey2 = new ASKey("TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz", "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU");
        CompletableFuture<String> future = binanceRestHttpApiClient.queryBinanceApi(
                RunEnv.NORMAL,
                TradeType.SPOT,
                BinanceRestApiType.QUERY_LISTEN_KEY,
                new JSONObject(),
                asKey2
        );

        System.out.println(future.get());
    }
}
