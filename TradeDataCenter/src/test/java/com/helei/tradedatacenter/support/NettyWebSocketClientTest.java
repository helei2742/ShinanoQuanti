package com.helei.tradedatacenter.support;

import com.helei.tradedatacenter.subscribe.binanceapi.BinanceWSApiClient;
import com.helei.tradedatacenter.netty.base.AbstractNettyClient;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;


@SpringBootTest
class NettyWebSocketClientTest {

    private AbstractNettyClient client;

    @Test
    public void start() throws InterruptedException {
        client = new BinanceWSApiClient("wss://fstream.binance.com", Arrays.asList("btcusdt@aggTrade"));
        try {
            client.connect();
            System.out.println("123");
            client.sendMessage("123");
        } catch (Exception e) {
            e.printStackTrace();
        }


//        TimeUnit.SECONDS.sleep(10000);
    }

    public void sendMessage(String message) {
        client.sendMessage(message);
    }

    @PreDestroy
    public void stop() {
        client.close();
    }


}
