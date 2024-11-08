package com.helei.binanceapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.AbstractBinanceWSApiClientHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BinanceWSReqRespApiClientHandler extends AbstractBinanceWSApiClientHandler {

    @Override
    protected void handleStreamMessage(String streamName, JSONObject content) {
        log.warn("非流式客户端,收到了错误的消息, streamName[{}]-content[{}]", streamName, content);
    }
}
