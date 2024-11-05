package com.helei.constants.order;

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
    LIMIT,
    /**
     * 市价单
     */
    MARKET,
    /**
     * 止损市价单
     */
    STOP_MARKET,
    /**
     * 限价止损单
     */
    STOP_LIMIT,
    /**
     * 止盈单
     */
    TAKE_PROFIT_MARKET,
    /**
     * 限价止盈单
     */
    TAKE_PROFIT_LIMIT,
    /**
     * 跟踪止损单
     */
    TRAILING_STIO_MARKET,
    ;
}
