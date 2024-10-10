package com.helei.cexapi;

import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClientHandler;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;

import java.net.URISyntaxException;

public class CEXApiFactory {

    public static BinanceWSApiClientClient binanceApiClient(
            int threadPoolSize,
            String url
    ) throws URISyntaxException {
        BinanceWSApiClientClientHandler handler = new BinanceWSApiClientClientHandler();

        return new BinanceWSApiClientClient(
                threadPoolSize,
                url,
                new IpWeightSupporter("localIp"),
                handler
        );
    }
}
