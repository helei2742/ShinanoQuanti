package com.helei.cexapi.binanceapi.api;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.cexapi.constants.BinanceWebSocketUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

class BinanceWSAccountApiTest {

    private static BinanceWSAccountApi binanceWSAccountApi;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        BinanceWSApiClient binanceWSApiClient = CEXApiFactory.binanceApiClient(1, BinanceWebSocketUrl.WS_NORMAL_URL);
        binanceWSAccountApi = binanceWSApiClient.getAccountApi();
        binanceWSApiClient.connect().get();
    }

    @Test
    void accountStatus() throws ExecutionException, InterruptedException {
        String ak = "TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz";
        String sk = "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU";

        binanceWSAccountApi.accountStatus(true, new ASKey(ak, sk)).thenAccept(res -> {
                            System.out.println(res);
                        }
                )
                .get();
    }

    @Test
    void accountRateLimitsOrders() {
    }

    @Test
    void accountAllOrders() {
    }

    @Test
    void accountTrades() {
    }
}
