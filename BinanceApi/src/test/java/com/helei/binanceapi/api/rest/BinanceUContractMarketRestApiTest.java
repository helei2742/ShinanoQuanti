package com.helei.binanceapi.api.rest;

import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.constants.trade.KLineInterval;
import com.helei.dto.trade.KLine;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

class BinanceUContractMarketRestApiTest {
    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);

    @Test
    void queryKLines() throws ExecutionException, InterruptedException {
        String wsMarketUrl = BinanceApiConfig.INSTANCE.getNormal().getU_contract().getRest_api_url();
        BinanceUContractMarketRestApi binanceUContractMarketRestApi = new BinanceUContractMarketRestApi(
                Executors.newSingleThreadExecutor(),
                wsMarketUrl,
                new IpWeightSupporter(wsMarketUrl)
        );
        binanceUContractMarketRestApi.setProxy(proxy);

        CompletableFuture<List<KLine>> future = binanceUContractMarketRestApi.queryKLines(
                "btcusdt",
                KLineInterval.d_1,
                LocalDateTime.of(2021, 1, 1, 1, 1).toInstant(ZoneOffset.UTC).toEpochMilli(),
                null,
                200
        );
        List<KLine> kLines = future.get();
        System.out.println(kLines);
        System.out.println(kLines.size());
    }
}
