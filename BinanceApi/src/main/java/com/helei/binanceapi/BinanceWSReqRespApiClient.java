package com.helei.binanceapi;

import com.helei.binanceapi.api.ws.*;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

/**
 * 币安Websocket，请求-响应模式的api客户端
 */
@Getter
@Slf4j
public class BinanceWSReqRespApiClient extends AbstractBinanceWSApiClient {

    /**
     * 基础的api
     */
    private final BinanceWSBaseApi baseApi;

    /**
     * 行情相关的api
     */
    private final BinanceWSMarketApi marketApi;

    /**
     * 交易相关api
     */
    private final BinanceWSTradeApi tradeApi;

    /**
     * 账户相关api
     */
    private final BinanceWSAccountApi accountApi;

    public BinanceWSReqRespApiClient(
            String url,
            IpWeightSupporter ipWeightSupporter
    ) throws URISyntaxException {
        super(BinanceWSClientType.REQUEST_RESPONSE, url, ipWeightSupporter, new BinanceWSReqRespApiClientHandler());
        baseApi = new BinanceWSBaseApi(this);
        marketApi = new BinanceWSMarketApi(this);
        tradeApi = new BinanceWSTradeApi(this);
        accountApi = new BinanceWSAccountApi(this);
    }
}
