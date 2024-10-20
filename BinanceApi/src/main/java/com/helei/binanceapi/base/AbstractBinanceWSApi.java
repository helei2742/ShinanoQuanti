package com.helei.binanceapi.base;

import com.helei.binanceapi.BinanceWSApiClient;

import java.net.URISyntaxException;

/**
 * 币安接口基础类
 */
public class AbstractBinanceWSApi {
    protected final BinanceWSApiClient binanceWSApiClient;

    public AbstractBinanceWSApi(
            BinanceWSApiClient binanceWSApiClient
    ) throws URISyntaxException {
        this.binanceWSApiClient = binanceWSApiClient;
    }


}
