package com.helei.binanceapi;

import com.helei.binanceapi.api.ws.*;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;


/**
 * 币安市场信息推送流客户端
 */
@Getter
@Slf4j
public class BinanceWSMarketStreamClient extends AbstractBinanceWSApiClient {


    /**
     * stream流推送相关api
     */
    private final BinanceWSStreamApi streamApi;


    public BinanceWSMarketStreamClient(
            String url,
            IpWeightSupporter ipWeightSupporter
    ) throws URISyntaxException {
        super(BinanceWSClientType.MARKET_STREAM, url, ipWeightSupporter, new BinanceWSMarketStreamClientHandler());
        streamApi = new BinanceWSStreamApi(this);
    }
}
