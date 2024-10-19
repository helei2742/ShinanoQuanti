package com.helei.tradedatacenter.support;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.constants.BinanceApiUrl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class WebSocketClientTest {


    @Test
    public void testClientPong() throws URISyntaxException, SSLException, InterruptedException {

        BinanceWSApiClient binanceWSApiClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);

        binanceWSApiClient.connect();

        TimeUnit.SECONDS.sleep(100);
    }
}
