package com.helei.binanceapi.base;


import java.net.URISyntaxException;

/**
 * 币安接口基础类
 */
public class AbstractBinanceWSApi {
    protected final AbstractBinanceWSApiClient binanceWSApiClient;

    public AbstractBinanceWSApi(
            AbstractBinanceWSApiClient binanceWSApiClient
    ) throws URISyntaxException {
        this.binanceWSApiClient = binanceWSApiClient;
    }


}
