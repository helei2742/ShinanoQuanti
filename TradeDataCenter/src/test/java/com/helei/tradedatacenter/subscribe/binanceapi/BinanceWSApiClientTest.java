package com.helei.tradedatacenter.subscribe.binanceapi;

import com.helei.tradedatacenter.subscribe.binanceapi.constants.WebSocketCommandType;
import com.helei.tradedatacenter.subscribe.binanceapi.constants.WebSocketUrl;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketCommand;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BinanceWSApiClientTest {


    @Test
    public void testBinanceWSApiClient() {
        BinanceWSApiClientHandler handler = new BinanceWSApiClientHandler();
        BinanceWSApiClient binanceWSApiClient = null;
        try {
            binanceWSApiClient = new BinanceWSApiClient(WebSocketUrl.NORMAL_URL, handler);
            binanceWSApiClient.connect();

            binanceWSApiClient.sendRequest(WebSocketCommand.builder().setCommandType(WebSocketCommandType.GET_PROPERTY).build());
            TimeUnit.SECONDS.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
