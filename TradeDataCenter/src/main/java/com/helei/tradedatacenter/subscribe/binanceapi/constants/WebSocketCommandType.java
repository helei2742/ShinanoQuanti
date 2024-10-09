package com.helei.tradedatacenter.subscribe.binanceapi.constants;

import lombok.Getter;

@Getter
public enum WebSocketCommandType {

    SUBSCRIBE("SUBSCRIBE"),
    UNSUBSCRIBE("UNSUBSCRIBE"),
    LIST_SUBSCRIPTIONS("LIST_SUBSCRIPTIONS"),
    SET_PROPERTY("SET_PROPERTY"),
    GET_PROPERTY("GET_PROPERTY"),
    PING("ping"),
    PONG("pong");

    WebSocketCommandType(String description) {
        this.description = description;
    }

    private final String description;

}