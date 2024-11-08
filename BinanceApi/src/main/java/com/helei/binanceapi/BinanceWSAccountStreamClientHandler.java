package com.helei.binanceapi;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.AbstractBinanceWSApiClientHandler;
import com.helei.binanceapi.constants.AccountEventType;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 币安账户信息流客户端处理消息的handler
 */
@Setter
@Slf4j
class BinanceWSAccountStreamClientHandler extends AbstractBinanceWSApiClientHandler {

    @Getter
    private Consumer<AccountEvent> whenReceiveEvent;


    public BinanceWSAccountStreamClientHandler(Consumer<AccountEvent> whenReceiveEvent) {
        this.whenReceiveEvent = whenReceiveEvent;
    }

    public BinanceWSAccountStreamClientHandler() {}

    @Override
    protected void handleResponseMessage(String id, JSONObject response) {
        log.warn("get an response, not stream message! [{}]", response);
    }

    @Override
    protected void handleStreamMessage(String streamName, JSONObject context) {
        if (whenReceiveEvent != null) {
            whenReceiveEvent.accept(AccountEventType.STATUS_MAP.get(context.getString("e")).getConverter().convertFromJsonObject(context));
        }
    }
}
