package com.helei.tradedatacenter.subscribe.binanceapi;

import com.helei.tradedatacenter.netty.base.AbstractWebSocketClientHandler;
import com.helei.tradedatacenter.netty.base.AbstractWebsocketClient;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketCommand;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketResponse;
import lombok.extern.slf4j.Slf4j;


import java.net.URISyntaxException;


/**
 * 币安ws接口客户端
 */
@Slf4j
public class BinanceWSApiClient extends AbstractWebsocketClient<WebSocketCommand, WebSocketResponse> {


    public BinanceWSApiClient(String url, AbstractWebSocketClientHandler<WebSocketCommand, WebSocketResponse> handler) throws URISyntaxException {
        super(url, handler);
    }


}
