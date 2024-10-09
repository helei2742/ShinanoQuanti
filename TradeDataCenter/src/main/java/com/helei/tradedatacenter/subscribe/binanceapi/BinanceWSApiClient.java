package com.helei.tradedatacenter.subscribe.binanceapi;

import com.helei.tradedatacenter.netty.base.AbstractWebSocketClientHandler;
import com.helei.tradedatacenter.netty.base.AbstractWebsocketClient;
import lombok.extern.slf4j.Slf4j;


import java.net.URISyntaxException;


/**
 * 币安ws接口客户端
 */
@Slf4j
public class BinanceWSApiClient extends AbstractWebsocketClient {


    public BinanceWSApiClient(String url, AbstractWebSocketClientHandler handler) throws URISyntaxException {
        super(url, handler);

        handler.set()
    }


}
