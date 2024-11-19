package com.helei;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.solanarpc.SolanaRestHttpClient;
import com.helei.solanarpc.SolanaWebsocketClient;
import com.helei.solanarpc.constants.SolanaCommitment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
class SolanaWebsocketClientTest {

    private static final String SOLANA_WS_URL = "wss://api.mainnet-beta.solana.com";
    private static final String SOLANA_HTTP_URL = "https://api.mainnet-beta.solana.com";

    private static final InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);


    private SolanaWebsocketClient solanaWebsocketClient;

    private SolanaRestHttpClient solanaRestHttpClient;

    private final List<String> addressList = new ArrayList<>();


    @BeforeEach
    void setUp() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        addressList.add("C3nLTNMK6Ao1s3J1CQhv8GbT3NoMmifWoi9PGEcYd9hP");
        addressList.add("28hGFUJPqqHv1wLhkC6xfh4Bf3zsxpeydFWZGHuGaMwk");
        addressList.add("AxThFwuQ1Vf5FMSmmBv3vb2DXRGPFkHjKbNZVs4YMx4X");
        addressList.add("7VBTpiiEjkwRbRGHJFUz6o5fWuhPFtAmy8JGhNqwHNnn");
        addressList.add("EBnKTDJxUCPzLQDbFE3gsvpiBwq9UiR5NWja6EZtSw3z");

        solanaWebsocketClient = new SolanaWebsocketClient(SOLANA_WS_URL);
        solanaWebsocketClient.setProxy(proxy);
        solanaWebsocketClient.connect().get();

        solanaRestHttpClient = new SolanaRestHttpClient(null, SOLANA_HTTP_URL, Executors.newFixedThreadPool(2));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void sendSolanaWSRequest() throws InterruptedException {

        for (String address : addressList) {
            solanaWebsocketClient.logsSubscribe(address, (eventType, context, event) -> {

                log.info("[{}]收到事件[{}]", context.getAddress(), event);
                JSONObject value = event.getJSONObject("params").getJSONObject("result").getJSONObject("value");

                // 1.监听事件获取签名
                String signature = value.getString("signature");

                // 2.根据签名后去交易信息
                solanaRestHttpClient.getTransaction(signature).thenAcceptAsync(response->{
                    log.info("交易信息[{}]", response);
                    JSONObject result = response.getJSONObject("result");
                    JSONObject transaction = result.getJSONObject("transaction");
                    JSONObject meta = result.getJSONObject("meta");

                    JSONArray addressArray = transaction.getJSONObject("message").getJSONArray("accountKeys");
                    JSONArray preBalances = meta.getJSONArray("preBalances");
                    JSONArray postBalances = meta.getJSONArray("postBalances");

                });
            }).thenAcceptAsync(subId->{
                log.info("订阅ID[{}]", subId);
            });
        }


        TimeUnit.MINUTES.sleep(100);
    }
}
