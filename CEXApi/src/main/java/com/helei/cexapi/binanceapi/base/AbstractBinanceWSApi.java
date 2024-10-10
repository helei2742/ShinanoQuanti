package com.helei.cexapi.binanceapi.base;

import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;

import java.net.URISyntaxException;

/**
 * 币安接口基础类
 */
public class AbstractBinanceWSApi {
    protected final BinanceWSApiClientClient binanceWSApiClient;

    public AbstractBinanceWSApi(
            BinanceWSApiClientClient binanceWSApiClient
    ) throws URISyntaxException {
        this.binanceWSApiClient = binanceWSApiClient;
    }


}
