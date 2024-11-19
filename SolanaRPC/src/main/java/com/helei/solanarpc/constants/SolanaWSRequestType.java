package com.helei.solanarpc.constants;

/**
 * Solana Json Rpc Api Websocket api method type
 */
public enum SolanaWSRequestType {

    /**
     * 订阅账号事件
     */
    accountSubscribe,
    /**
     * 取消订阅账号事件
     */
    accountUnsubscribe,
    /**
     * 订阅日志事件
     */
    logsSubscribe,
    /**
     * 取消订阅日志事件
     */
    logsUnsubscribe,

    /**
     * 订阅程序事件
     */
    programSubscribe,
    /**
     * 取消订阅程序事件
     */
    programUnsubscribe,
    /**
     * 订阅签名事件
     */
    signatureSubscribe,

    /**
     * 取消订阅签名事件
     */
    signatureUnsubscribe,
    /**
     * 订阅槽位事件
     */
    slotSubscribe,
    /**
     * 取消订阅槽位事件
     */
    slotUnsubscribe,
}
