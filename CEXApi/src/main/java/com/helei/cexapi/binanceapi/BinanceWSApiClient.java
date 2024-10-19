package com.helei.cexapi.binanceapi;

import com.helei.cexapi.binanceapi.api.ws.*;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApiClientHandler;
import com.helei.cexapi.binanceapi.supporter.BinanceWSStreamSupporter;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;


/**
 * 币安ws接口客户端
 */
@Getter
@Slf4j
public class BinanceWSApiClient extends AbstractBinanceWSApiClient {


    /**
     * 基础的api
     */
    private final BinanceWSBaseApi baseApi;

    /**
     * 行情相关的api
     */
    private final BinanceWSMarketApi marketApi;

    /**
     * stream流推送相关api
     */
    private final BinanceWSStreamApi streamApi;

    /**
     * 交易相关api
     */
    private final BinanceWSTradeApi tradeApi;

    /**
     * 现货账户api
     */
    private final BinanceWSSpotAccountApi spotAccountApi;

    /**
     * 合约账户api
     */
    private final BinanceWSContractAccountApi contractAccountApi;

    public BinanceWSApiClient(
            String url,
            IpWeightSupporter ipWeightSupporter,
            AbstractBinanceWSApiClientHandler handler
    ) throws URISyntaxException, SSLException {
        super(url, ipWeightSupporter, new BinanceWSStreamSupporter(), handler);
        baseApi = new BinanceWSBaseApi(this);
        marketApi = new BinanceWSMarketApi(this);
        streamApi = new BinanceWSStreamApi(this);
        tradeApi = new BinanceWSTradeApi(this);
        spotAccountApi = new BinanceWSSpotAccountApi(this);
        contractAccountApi = new BinanceWSContractAccountApi(this);
    }
}