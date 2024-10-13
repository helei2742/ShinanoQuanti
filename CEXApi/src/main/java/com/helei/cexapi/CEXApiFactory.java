package com.helei.cexapi;

import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.BinanceWSApiClientHandler;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;

public class CEXApiFactory {

    //TODO 配置化
    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7897);
//    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);
//    private static InetSocketAddress proxy = null;

    public static BinanceWSApiClient binanceApiClient(
            int threadPoolSize,
            String url
    ) throws URISyntaxException {
        BinanceWSApiClientHandler handler = new BinanceWSApiClientHandler();

        return new BinanceWSApiClient(
                threadPoolSize,
                url,
                proxy,
                new IpWeightSupporter("localIp"),
                handler
        );
    }
}
