package com.helei.tradesignalcenter.support;

import com.helei.cexapi.CEXApiFactory;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceApiUrl;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WebSocketClientTest {


    @Test
    public void testClientPong() throws URISyntaxException, SSLException, InterruptedException {

        BinanceWSApiClient binanceWSApiClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);

        binanceWSApiClient.connect();

        TimeUnit.SECONDS.sleep(100);
    }
}
