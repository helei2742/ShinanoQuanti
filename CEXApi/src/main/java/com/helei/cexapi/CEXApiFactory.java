package com.helei.cexapi;


import com.helei.binanceapi.api.rest.BinanceUContractMarketRestApi;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.dto.config.RunTypeConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
public class CEXApiFactory {

    private static final String CLIENT_CONNECT_KEY_SPACER = "<@>?";

    private static final Map<RunTypeConfig, BinanceBaseClientManager> MANAGER_MAP = new ConcurrentHashMap<>();

    public static BinanceBaseClientManager binanceBaseWSClientManager(RunTypeConfig runTypeConfig, ExecutorService executor) {

        return MANAGER_MAP.compute(runTypeConfig, (k, v) -> {
            if (v == null) {
                v = new BinanceBaseClientManager(runTypeConfig, executor);
            }
            return v;
        });
    }


    public static BinanceUContractMarketRestApi binanceUContractMarketRestApi(String baseUrl,
                                                                              ExecutorService executor) {
        return new BinanceUContractMarketRestApi(executor, baseUrl, new IpWeightSupporter("123123"));
    }


    /**
     * 创建客户端链接的key，保证同一个名和url只有一个链接
     *
     * @param url  url
     * @param name name
     * @return String
     */
    private static String buildClientConnectKey(String url, String name) {
        return name + CLIENT_CONNECT_KEY_SPACER + url;
    }
}


