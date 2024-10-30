package com.helei.cexapi;


import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.BinanceWSApiClientHandler;
import com.helei.binanceapi.api.rest.BinanceUContractMarketRestApi;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Slf4j
public class CEXApiFactory {

    private static final String CLIENT_CONNECT_KEY_SPACER = "<@>?";
    //TODO 配置化
//    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7897);
    private static InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 7890);
//    private static InetSocketAddress proxy = null;

    private static IpWeightSupporter ipWeightSupporter = new IpWeightSupporter("localIp");

    private static final ConcurrentMap<String, BinanceWSApiClient> CLIENT_CONNECTION_MAP = new ConcurrentHashMap<>();

    public static BinanceWSApiClient binanceApiClient(
            String url,
            String name
    ) {
        String clientKey = buildClientConnectKey(url, name);

        BinanceWSApiClient binanceWSApiClient = CLIENT_CONNECTION_MAP.get(clientKey);
        if (binanceWSApiClient != null) {
            log.info("已存在链接客户端 [{}] - [{}]", name, url);
            return binanceWSApiClient;
        }

        BinanceWSApiClient client;
        synchronized (CEXApiFactory.class) {
            try {
                log.info("创建链接客户端 [{}] - [{}]", name, url);

                client = binanceApiClient(url);
                client.setName(name);
                CLIENT_CONNECTION_MAP.put(clientKey, client);

                log.info("创建链接客户端 [{}] - [{}] 成功", name, url);
            } catch (Exception e) {
                log.error("创建链接客户端 [{}] - [{}] 失败", name, url, e);
                throw new RuntimeException("创建连接客户端失败", e);
            }
        }
        return client;
    }

    protected static BinanceWSApiClient binanceApiClient(
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

    public static BinanceUContractMarketRestApi binanceUContractMarketRestApi(String baseUrl,
                                                                              ExecutorService executor) {
        return new BinanceUContractMarketRestApi(executor, baseUrl, ipWeightSupporter);
    }


    /**
     * 创建客户端链接的key，保证同一个名和url只有一个链接
     * @param url url
     * @param name name
     * @return String
     */
    private static String buildClientConnectKey(String url, String name) {
        return name + CLIENT_CONNECT_KEY_SPACER + url;
    }
}
