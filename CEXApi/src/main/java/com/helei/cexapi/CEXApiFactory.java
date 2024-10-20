package com.helei.cexapi;


import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.BinanceWSApiClientHandler;
import com.helei.binanceapi.supporter.IpWeightSupporter;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

public class CEXApiFactory {

    //TODO 配置化
//    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7897);
    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);
//    private static InetSocketAddress proxy = null;

    public static BinanceWSApiClient binanceApiClient(
            String url
    ) throws URISyntaxException, SSLException {
        BinanceWSApiClientHandler handler = new BinanceWSApiClientHandler();

        BinanceWSApiClient client = new BinanceWSApiClient(
                url,
                new IpWeightSupporter("localIp"),
                handler
        );
        client.setProxy(proxy);
        return client;
    }
}
