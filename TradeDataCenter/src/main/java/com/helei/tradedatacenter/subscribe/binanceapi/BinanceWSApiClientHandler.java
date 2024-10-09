package com.helei.tradedatacenter.subscribe.binanceapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketCommand;
import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketResponse;
import com.helei.tradedatacenter.netty.base.AbstractWebSocketClientHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


/**
 * BinanceWSApiClient的消息处理器
 */
@Slf4j
public class BinanceWSApiClientHandler extends AbstractWebSocketClientHandler {
    @Override
    protected void receiveMessage(String text) {
        WebSocketResponse response = JSONObject.parseObject(text, WebSocketResponse.class);

        Integer status = response.getStatus();

        switch (status) {
            case 200:

                break;
            default:
                log.error("response error, status code [{}}", status);
                break;
        }
    }


    @Override
    protected void sendPing(ChannelHandlerContext ctx) {
        websocketClient.sendMessage((WebSocketCommand.builder().buildPing()));
    }

    @Override
    protected void sendPong(ChannelHandlerContext ctx, String id) {

    }
}