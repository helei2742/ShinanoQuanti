package com.helei.binanceapi.api.rest;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.constants.api.BinanceRestApiType;
import com.helei.constants.RunEnv;
import com.helei.dto.trade.KLine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class BinanceRestHttpApiClientTest {

    @Test
    void queryBinanceApi() throws ExecutionException, InterruptedException {

        BinanceRestHttpApiClient binanceRestHttpApiClient = new BinanceRestHttpApiClient(BinanceApiConfig.INSTANCE, Executors.newVirtualThreadPerTaskExecutor());

        CompletableFuture<List<KLine>> stringCompletableFuture = binanceRestHttpApiClient.queryBinanceApi(
                RunEnv.TEST_NET,
                BinanceRestApiType.SPOT_KLINE,
                new JSONObject(),
                null
        );

        List<KLine> kLines = stringCompletableFuture.get();

        System.out.println(kLines);


        CompletableFuture<String> future = binanceRestHttpApiClient.queryBinanceApi(
                RunEnv.NORMAL,
                BinanceRestApiType.QUERY_LISTEN_KEY,
                new JSONObject(),
                null
        );

        System.out.println(future.get());
    }
}
