package com.helei.cexapi.binanceapi.constants;

import lombok.Getter;

@Getter
public enum WebSocketCommandType {
    /**
     * 订阅
     */
    SUBSCRIBE("SUBSCRIBE"),
    /**
     * 取消订阅
     */
    UNSUBSCRIBE("UNSUBSCRIBE"),
    /**
     * 当前订阅
     */
    LIST_SUBSCRIPTIONS("LIST_SUBSCRIPTIONS"),
    /**
     * 设置属性
     */
    SET_PROPERTY("SET_PROPERTY"),
    /**
     * 获取属性
     */
    GET_PROPERTY("GET_PROPERTY"),
    /**
     * ping
     */
    PING("ping"),
    /**
     * ping
     */
    PONG("pong"),
    /**
     * 获取服务器时间
     */
    TIME("time"),
    /**
     * 获取交易规范信息
     */
    EXCHANGE_INFO("exchangeInfo"),
    /**
     * 获取深度信息
     */
    DEPTH("depth"),
    TRADES_RECENT("trades.recent"),

    /**
     * 历史k线数据
     */
    KLINES("klines")
    ;

    WebSocketCommandType(String description) {
        this.description = description;
    }

    private final String description;

}
