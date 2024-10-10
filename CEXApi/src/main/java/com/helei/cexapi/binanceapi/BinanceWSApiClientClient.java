

package com.helei.cexapi.binanceapi;

import com.helei.cexapi.binanceapi.api.BinanceWSBaseApi;
import com.helei.cexapi.binanceapi.api.BinanceWSSpotApi;
import com.helei.cexapi.binanceapi.api.BinanceWSStreamApi;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApiClientHandler;
import com.helei.cexapi.binanceapi.supporter.BinanceWSStreamSupporter;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


import java.net.URISyntaxException;


/**
 * 币安ws接口客户端
 */
@Getter
@Slf4j
public class BinanceWSApiClientClient extends AbstractBinanceWSApiClient {


    /**
     * 基础的api
     */
    private final BinanceWSBaseApi baseApi;

    /**
     * 现货相关的api
     */
    private final BinanceWSSpotApi spotApi;

    /**
     * stream流推送相关api
     */
    private final BinanceWSStreamApi streamApi;

    public BinanceWSApiClientClient(
            int threadPoolSize,
            String url,
            IpWeightSupporter ipWeightSupporter,
            AbstractBinanceWSApiClientHandler handler
    ) throws URISyntaxException {
        super(threadPoolSize, url, ipWeightSupporter, new BinanceWSStreamSupporter(), handler);

        baseApi = new BinanceWSBaseApi(this);
        spotApi = new BinanceWSSpotApi(this);
        streamApi = new BinanceWSStreamApi(this);
    }
}
