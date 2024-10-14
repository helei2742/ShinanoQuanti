package com.helei.cexapi.binanceapi.constants.command;

import lombok.Getter;


@Getter
public enum MarketCommandType implements WSCommandType{

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


    MarketCommandType(String description) {
        this.description = description;
    }

    private final String description;

    @Override
    public String toString() {
        return description;
    }
}
