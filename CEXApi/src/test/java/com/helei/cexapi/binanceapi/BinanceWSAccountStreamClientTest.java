package com.helei.cexapi.binanceapi;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;
import com.helei.cexapi.constants.BinanceApiUrl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BinanceWSAccountStreamClientTest {

    private static BinanceWSApiClient apiClient;

    private static BinanceWSAccountStreamClient binanceWSAccountStreamClient;
    private static ASKey asKey;

    private static volatile String listenKey;

    @BeforeAll
    public static void beforAll() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        apiClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);
        apiClient.connect().get();
        String ak = "TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz";
        String sk = "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU";
        asKey = new ASKey(ak, sk);

        InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7897);

        binanceWSAccountStreamClient = new BinanceWSAccountStreamClient(
                BinanceApiUrl.WS_U_CONTRACT_STREAM_URL,
                new IpWeightSupporter(BinanceApiUrl.WS_U_CONTRACT_STREAM_URL),
                asKey,
                100,
                apiClient.getBaseApi()
        );

        binanceWSAccountStreamClient.setProxy(proxy);
    }

    @Test
    @Order(1)
    void requestListenKey() throws ExecutionException, InterruptedException {
        String s = apiClient.getBaseApi().requestListenKey(asKey).get();
        System.out.println("获取listenKey成功 ： ");
        System.out.println(s);
        listenKey = s;
        System.out.println("");
    }

    @Test
    @Order(2)
    void lengthenListenKey() throws ExecutionException, InterruptedException {
        String s = apiClient.getBaseApi().lengthenListenKey(listenKey, asKey).get();
        System.out.println("延长listenKey成功 ： ");
        System.out.println(s);
        System.out.println("");
    }

    @Test
    @Order(3)
    void closeListenKey() throws ExecutionException, InterruptedException {
        Boolean s = apiClient.getBaseApi().closeListenKey(listenKey, asKey).get();
        System.out.println("关闭listenKey成功 ： ");
        System.out.println(s);
        System.out.println("");
    }

    @Test
    void startAccountInfoStream() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> booleanCompletableFuture = binanceWSAccountStreamClient.startAccountInfoStream();

        Boolean b = booleanCompletableFuture.get();

        System.out.println("开启信息流结果： " + b);


        binanceWSAccountStreamClient.
    }
}