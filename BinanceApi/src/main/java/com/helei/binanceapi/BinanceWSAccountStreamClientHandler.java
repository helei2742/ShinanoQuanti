package com.helei.binanceapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.AbstractBinanceWSApiClientHandler;
import com.helei.util.CustomBlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * 币安账户信息流客户端处理消息的handler
 */
@Slf4j
class BinanceWSAccountStreamClientHandler extends AbstractBinanceWSApiClientHandler {
    /**
     * 账户信息流的缓冲区
     */
    private final CustomBlockingQueue<JSONObject> accountInfoBuffer;

    BinanceWSAccountStreamClientHandler(CustomBlockingQueue<JSONObject> accountInfoBuffer) {
        this.accountInfoBuffer = accountInfoBuffer;
    }

    @Override
    protected void whenReceiveMessage(String text) {
        JSONObject response = JSONObject.parseObject(text);
        if (response.get("id") != null) {
            log.warn("get an response, not stream message! [{}]", text);
        } else {
            log.debug("get stream message [{}}", text);
        }
        //TODO 判断返回值类型
        accountInfoBuffer.offer(response);
    }
}
