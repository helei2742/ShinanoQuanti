package com.helei.tradedatacenter.subscribe.binanceapi;

import com.helei.tradedatacenter.netty.base.AbstractNettyClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;

/**
 * 币安ws接口客户端
 */
@Slf4j
public class BinanceWSApiClient extends AbstractNettyClient {

    public BinanceWSApiClient(URI websocketURI) {
        super(websocketURI, 3 * 60, null);

    }

}
