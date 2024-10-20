package com.helei.cexapi.binanceapi.api;

import com.helei.cexapi.CEXApiFactory;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.api.ws.BinanceWSSpotAccountApi;
import com.helei.binanceapi.dto.ASKey;
import com.helei.binanceapi.constants.BinanceApiUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

class BinanceWSSpotAccountApiTest {

    private static BinanceWSSpotAccountApi binanceWSSpotAccountApi;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        BinanceWSApiClient binanceWSApiClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);
        binanceWSSpotAccountApi = binanceWSApiClient.getSpotAccountApi();
        binanceWSApiClient.connect().get();
    }

    @Test
    void accountStatus() throws ExecutionException, InterruptedException {
        String ak = "TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz";
        String sk = "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU";

        binanceWSSpotAccountApi.accountStatus(true, new ASKey(ak, sk)).thenAccept(res -> {
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
