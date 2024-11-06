package com.helei.constants.order;


/**
 * 订单状态
 */
public enum OrderStatus {
    /**
     * 订单被交易引擎接
     */
    NEW,
    /**
     * 部分订单被成交
     */
    PARTIALLY_FILLED,
    /**
     * 订单完全成交
     */
    FILLED,
    /**
     * 用户撤销了订单
     */
    CANCELED,
    /**
     * 订单没有被交易引擎接受，也没被处理
     */
    REJECTED,
    /**
     * 订单被交易引擎取消，比如：
     * 1.LIMIT FOK 订单没有成交
     * 2.市价单没有完全成交
     * 3.强平期间被取消的订单
     * 4.交易所维护期间被取消的订单
     */
    EXPIRED,
    /**
     * 表示订单由于 STP 触发而过期 （e.g. 带有 EXPIRE_TAKER 的订单与订单簿上属于同账户或同 tradeGroupId 的订单撮合）
     */
    EXPIRED_IN_MATCH,

    /**
     * 资金不足，导致无法下单
     */
    BALANCE_INSUFFICIENT,
}
