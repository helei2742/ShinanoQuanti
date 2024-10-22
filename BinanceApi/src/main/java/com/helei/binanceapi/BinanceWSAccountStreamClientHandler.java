package com.helei.binanceapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.AbstractBinanceWSApiClientHandler;
import com.helei.binanceapi.constants.AccountEventType;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 币安账户信息流客户端处理消息的handler
 */
@Slf4j
class BinanceWSAccountStreamClientHandler extends AbstractBinanceWSApiClientHandler {

    private final Consumer<AccountEvent> whenReceiveEvent;

    BinanceWSAccountStreamClientHandler( Consumer<AccountEvent> whenReceiveEvent) {
        this.whenReceiveEvent = whenReceiveEvent;
    }

    @Override
    protected void whenReceiveMessage(String text) {
        JSONObject response = JSONObject.parseObject(text);
        if (response.get("id") != null) {
            log.warn("get an response, not stream message! [{}]", text);
        } else if (response.get("e") != null){
            log.debug("get stream message [{}}", text);
            whenReceiveEvent.accept(AccountEventType.STATUS_MAP.get(response.getString("e")).getConverter().convertFromJsonObject(response));
        }
    }
}
