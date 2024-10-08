package com.helei.tradedatacenter.support;

import com.helei.tradedatacenter.subscribe.binanceapi.BinanceWSApiClient;
import com.helei.tradedatacenter.netty.base.AbstractNettyClient;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
class NettyWebSocketClientTest {

    private AbstractNettyClient client;

    @Test
    public void start() throws InterruptedException, URISyntaxException {
//        client = new BinanceWSApiClient(new URI(generalUri("wss://dstream.binance.com", Arrays.asList("btcusdt@aggTrade"))));
        client = new BinanceWSApiClient(new URI("wss://dstream.binance.com"));
        try {
            client.connect();
            System.out.println("123");
//            client.sendMessage("123");
        } catch (Exception e) {
            e.printStackTrace();
        }


//        TimeUnit.SECONDS.sleep(10000);
    }

    private String generalUri(String baseUrl, List<String> subscribeList) {
        StringBuilder uri = new StringBuilder(baseUrl);

        if (subscribeList.size() == 1) {
            uri.append("/ws").append(subscribeList.get(0));
        } else {
            uri.append("/stream?streams=");
            for (String name : subscribeList) {
                uri.append(name);
            }
        }

        return uri.toString();
    }

    @PreDestroy
    public void stop() {
        client.close();
    }


}
