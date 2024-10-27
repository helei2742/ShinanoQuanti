package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceApiUrl;
import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.client.BinanceAccountMergeClient;
import com.helei.dto.ASKey;
import com.helei.dto.account.AccountLocationConfig;
import com.helei.dto.account.UserInfo;
import com.helei.tradesignalcenter.dto.UserAccountEvent;
import com.helei.tradesignalcenter.service.AccountInfoService;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


class BinanceAccountEventSourceTest {

    private StreamExecutionEnvironment env;
    private StreamExecutionEnvironment env2;


    private static BinanceWSApiClient apiClient;

    private static BinanceAccountMergeClient accountMergeClient;

    private static AccountInfoService accountInfoService;

    private static ASKey asKey;

    private static volatile String listenKey;

    @BeforeAll
    public static void beforAll() throws Exception {
//        apiClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL_TEST);
//        apiClient.connect().get();
        String ak = "b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66";
        String sk = "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f";

        asKey = new ASKey(ak, sk);

        InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);

//        accountInfoService = new AccountInfoService();
//
//        accountInfoService.getUid2UserInfo().put("test1", new UserInfo("test1", asKey, List.of("BTCUSDT", "ETHUSDT"), new AccountLocationConfig(0.2, 10, 100)));
//
//        accountMergeClient = new BinanceAccountMergeClient(apiClient, BinanceApiUrl.WS_ACCOUNT_INFO_STREAM_URL_TEST);
    }

    @Test
    public void testAccountEventSource() throws Exception {

        BinanceAccountEventSource eventSource = new BinanceAccountEventSource(asKey, BinanceApiUrl.WS_NORMAL_URL_TEST);

        DataStreamSource<UserAccountEvent> accountEventDataStream = env.addSource(eventSource);

        accountEventDataStream.print();

        env.execute("testAccountEventSource");
        TimeUnit.MINUTES.sleep(100);
    }
}

