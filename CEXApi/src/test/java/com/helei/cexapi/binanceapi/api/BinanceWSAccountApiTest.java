package com.helei.cexapi.binanceapi.api;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.cexapi.constants.WebSocketUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class BinanceWSAccountApiTest {

    private static BinanceWSAccountApi binanceWSAccountApi;

    @BeforeAll
    private static void beforeAll() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        BinanceWSApiClient binanceWSApiClient = CEXApiFactory.binanceApiClient(1, WebSocketUrl.WS_NORMAL_URL);
        binanceWSAccountApi = binanceWSApiClient.getAccountApi();
        binanceWSApiClient.connect().get();
    }

    @Test
    void accountStatus() throws ExecutionException, InterruptedException {
        String sk = "1a6QYWVo7zGY5gqEAik6pvPKjANAj9C74OA1dS1Xid8m4rELFHxiwt9HwEUtmiNT";
        String ak = "WHTQC67a0iRyKV0TosUxspQcHKRY3o13IKCnNoLLSceWMLYKE1yi4Cvn5HCHorAX";

        binanceWSAccountApi.accountStatus(true, new ASKey(sk, ak)).thenAccept(res -> {
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