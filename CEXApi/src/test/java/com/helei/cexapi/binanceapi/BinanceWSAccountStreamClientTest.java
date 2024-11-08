package com.helei.cexapi.binanceapi;

import com.helei.binanceapi.BinanceWSAccountEventStreamClient;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.config.RunTypeConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

        import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BinanceWSAccountStreamClientTest {

    private static RunEnv runEnv = RunEnv.NORMAL;

    private static TradeType tradeType = TradeType.CONTRACT;

    private static BinanceBaseClientManager clientManager;

    private static BinanceWSReqRespApiClient reqRespApiClient;

    private static ASKey asKey;

    private static BinanceWSAccountEventStreamClient accountEventStreamClient;


    private static volatile String listenKey;

    @BeforeAll
    public static void beforAll() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {

        //        String ak = "TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz";
//        String sk = "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU";
        //spot test
//        String ak = "1JIhkPyK07xadG9x8hIwqitN95MgpypPzA4b6TLraTonRnJ8BBJQlaO2iL9tPH0Y";
//        String sk = "t84TYFR1zieMGncbw3kYq4zAPLxIJHJeMdD8V0FMKxij9fApojV6bhbDpyyjNDWt";

        String ak = "b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66";
        String sk = "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f";
        asKey = new ASKey(ak, sk);

        clientManager = CEXApiFactory.binanceBaseWSClientManager(RunTypeConfig.DEFAULT_RUN_TYPE_CONFIG, Executors.newVirtualThreadPerTaskExecutor());

        reqRespApiClient = (BinanceWSReqRespApiClient) clientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.REQUEST_RESPONSE).get();

        accountEventStreamClient = (BinanceWSAccountEventStreamClient) clientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.ACCOUNT_STREAM).get();
    }

    @Test
    @Order(1)
    void requestListenKey() throws ExecutionException, InterruptedException {
        String s = reqRespApiClient.getBaseApi().requestListenKey(asKey).get();
        System.out.println("获取listenKey成功 ： ");
        System.out.println(s);
        listenKey = s;
        System.out.println("");
    }

    @Test
    @Order(2)
    void lengthenListenKey() throws ExecutionException, InterruptedException {
        String s = reqRespApiClient.getBaseApi().lengthenListenKey(listenKey, asKey).get();
        System.out.println("延长listenKey成功 ： ");
        System.out.println(s);
        System.out.println("");
    }

    @Test
    @Order(3)
    void closeListenKey() throws ExecutionException, InterruptedException {
        Boolean s = reqRespApiClient.getBaseApi().closeListenKey(listenKey, asKey).get();
        System.out.println("关闭listenKey成功 ： ");
        System.out.println(s);
        System.out.println("");
    }

    @Test
    void startAccountInfoStream() throws Exception {
        accountEventStreamClient.startAccountEventStream(listenKey, new UserAccountInfo(), accountEvent -> {
            System.out.println(accountEvent);
        });


        TimeUnit.MINUTES.sleep(100);
    }
}
