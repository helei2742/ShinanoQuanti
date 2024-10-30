package com.helei.cexapi.binanceapi;

import com.helei.binanceapi.BinanceWSAccountStreamClient;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.binanceapi.dto.accountevent.GridUpdateEvent;
import com.helei.binanceapi.dto.accountevent.OrderTradeUpdateEvent;
import com.helei.cexapi.CEXApiFactory;
import com.helei.dto.ASKey;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.binanceapi.constants.BinanceApiUrl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BinanceWSAccountStreamClientTest {

    private static BinanceWSApiClient apiClient;

    private static BinanceWSAccountStreamClient binanceWSAccountStreamClient;

    private static ASKey asKey;

    private static volatile String listenKey;

    @BeforeAll
    public static void beforAll() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        apiClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL_TEST, "BinanceWSAccountStreamClientTest");
        apiClient.connect().get();
//        String ak = "TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz";
//        String sk = "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU";
        //spot test
//        String ak = "1JIhkPyK07xadG9x8hIwqitN95MgpypPzA4b6TLraTonRnJ8BBJQlaO2iL9tPH0Y";
//        String sk = "t84TYFR1zieMGncbw3kYq4zAPLxIJHJeMdD8V0FMKxij9fApojV6bhbDpyyjNDWt";

        String ak = "b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66";
        String sk = "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f";
        asKey = new ASKey(ak, sk);

        InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);

        binanceWSAccountStreamClient = new BinanceWSAccountStreamClient(
                BinanceApiUrl.WS_ACCOUNT_INFO_STREAM_URL_TEST,
                new IpWeightSupporter(BinanceApiUrl.WS_U_CONTRACT_STREAM_URL),
                asKey,
                System.out::println,
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
    void startAccountInfoStream() throws Exception {
        CompletableFuture<Boolean> booleanCompletableFuture = binanceWSAccountStreamClient.startAccountInfoStream();

        Boolean b = booleanCompletableFuture.get();

        System.out.println("开启信息流结果： " + b);

        AccountEvent accountEvent = null;

        TimeUnit.MINUTES.sleep(100);
    }
}
