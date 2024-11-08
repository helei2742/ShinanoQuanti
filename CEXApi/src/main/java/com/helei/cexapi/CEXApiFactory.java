package com.helei.cexapi;


import com.helei.binanceapi.api.rest.BinanceRestHttpApiClient;
import com.helei.binanceapi.config.BinanceApiConfig;
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

    private static volatile BinanceRestHttpApiClient binanceRestHttpApiClient;

    public static BinanceBaseClientManager binanceBaseWSClientManager(RunTypeConfig runTypeConfig, ExecutorService executor) {

        return MANAGER_MAP.compute(runTypeConfig, (k, v) -> {
            if (v == null) {
                v = new BinanceBaseClientManager(runTypeConfig, executor);
            }
            return v;
        });
    }


    public static BinanceRestHttpApiClient binanceRestHttpApiClient(BinanceApiConfig binanceApiConfig,
                                                                     ExecutorService executor) {

        if (binanceRestHttpApiClient == null) {
            synchronized (BinanceRestHttpApiClient.class) {
                if (binanceRestHttpApiClient == null) {
                    binanceRestHttpApiClient = new BinanceRestHttpApiClient(binanceApiConfig, executor);
                }
            }
        }

        return binanceRestHttpApiClient;
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


