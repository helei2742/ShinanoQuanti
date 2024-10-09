package com.helei.tradedatacenter.subscribe.binanceapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketCommand;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketResponse;
import com.helei.tradedatacenter.netty.base.AbstractWebSocketClientHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * BinanceWSApiClient的消息处理器
 */
@Slf4j
public class BinanceWSApiClientHandler extends AbstractWebSocketClientHandler<WebSocketCommand, WebSocketResponse> {

    @Override
    protected WebSocketResponse messageConvert(String text) {
        return JSONObject.parseObject(text, WebSocketResponse.class);
    }

    @Override
    protected String getIdFromMessage(WebSocketResponse response) {
        return response.getId();
    }

    @Override
    protected String getIdFromCommand(WebSocketCommand command) {
        return command.getId();
    }


    @Override
    protected WebSocketCommand getPing() {
        return WebSocketCommand.builder().buildPing();
    }

    @Override
    protected WebSocketCommand getPong() {
        return WebSocketCommand.builder().buildPong();
    }

}
