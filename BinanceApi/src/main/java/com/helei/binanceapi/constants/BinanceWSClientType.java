package com.helei.binanceapi.constants;


/**
 * Binance WS 客户端的类型
 */
public enum BinanceWSClientType {
    /**
     * 请求、响应类型客户端
     */
    REQUEST_RESPONSE,
    /**
     * 市场行情推送流客户端
     */
    MARKET_STREAM,
    /**
     * 账户信息推送流客户端
     */
    ACCOUNT_STREAM,
}
