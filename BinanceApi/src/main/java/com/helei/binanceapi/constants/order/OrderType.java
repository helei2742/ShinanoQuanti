package com.helei.binanceapi.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


/**
 * 订单类型
 */
@Getter
public enum OrderType {
    /**
     * 限价单
     */
    LIMIT("LIMIT"),
    /**
     * 市价单
     */
    MARKET("MARKET"),
    /**
     * 止损市价单
     */
    STOP_MARKET("STOP_MARKET"),
    /**
     * 限价止损单
     */
    STOP("STOP"),
    /**
     * 止盈单
     */
    TAKE_PROFIT_MARKET("TAKE_PROFIT_MARKET"),
    /**
     * 限价止盈单
     */
    TAKE_PROFIT_LIMIT("TAKE_PROFIT"),
    /**
     * 跟踪止损单
     */
    TRAILING_STIO_MARKET("TRAILING_STIO_MARKET"),
    ;

    public static final Map<String, OrderType> STATUS_MAP = new HashMap<>();

    static {
        for (OrderType status : OrderType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
