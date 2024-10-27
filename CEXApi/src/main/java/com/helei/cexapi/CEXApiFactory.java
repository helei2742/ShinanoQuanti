package com.helei.cexapi;


import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.BinanceWSApiClientHandler;
import com.helei.binanceapi.api.rest.BinanceUContractMarketRestApi;
import com.helei.binanceapi.supporter.IpWeightSupporter;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class CEXApiFactory {

    //TODO 配置化
//    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7897);
    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);
//    private static InetSocketAddress proxy = null;

    private static IpWeightSupporter ipWeightSupporter = new IpWeightSupporter("localIp");

    public static BinanceWSApiClient binanceApiClient(
            String url
    ) throws URISyntaxException, SSLException {
        BinanceWSApiClientHandler handler = new BinanceWSApiClientHandler();

        BinanceWSApiClient client = new BinanceWSApiClient(
                url,
                ipWeightSupporter,
                handler
        );
        client.setProxy(proxy);
        client.setName("binance-api-client-" + UUID.randomUUID().toString().substring(0, 8));
        return client;
    }

    public static BinanceWSApiClient binanceApiClient(
            String url,
            String name
    ) throws URISyntaxException, SSLException {
        BinanceWSApiClient client = binanceApiClient(url);
        client.setName(name);
        return client;
    }

    public static BinanceUContractMarketRestApi binanceUContractMarketRestApi(String baseUrl,
                                                                              ExecutorService executor) {
        return new BinanceUContractMarketRestApi(executor, baseUrl, ipWeightSupporter);
    }
}
