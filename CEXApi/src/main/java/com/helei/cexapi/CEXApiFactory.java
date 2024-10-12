package com.helei.cexapi;

import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClientHandler;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;

public class CEXApiFactory {

    //TODO 配置化
//    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7897);
    private static InetSocketAddress proxy = null;

    public static BinanceWSApiClientClient binanceApiClient(
            int threadPoolSize,
            String url
    ) throws URISyntaxException {
        BinanceWSApiClientClientHandler handler = new BinanceWSApiClientClientHandler();

        return new BinanceWSApiClientClient(
                threadPoolSize,
                url,
                proxy,
                new IpWeightSupporter("localIp"),
                handler
        );
    }
}
