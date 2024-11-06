package com.helei.snowflack;


import lombok.Getter;

@Getter
public enum BRStyle {
    BINANCE_MAIN_ORDER("binance", "币安订单"),
    TRADE_SIGNAL("trade-signal", "交易信号");

    private final String code;
    private final String info;

    BRStyle(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
