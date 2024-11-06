package com.helei.constants.order;


/**
 * 订单事件
 */
public enum OrderEvent {
    /**
     * 创建订单事件
     */
    CREATED_ORDER,
    /**
     * 发送到DB事件
     */
    SEND_TO_DB,
    /**
     * 发送到kafka事件
     */
    SEND_TO_KAFKA,
    /**
     * 重试发送到db事件
     */
    SEND_TO_DB_RETRY,
    /**
     * 重试发送到kafka事件
     */
    SEND_TO_KAFKA_RETRY,
    /**
     * 发送到db失败
     */
    SEND_TO_DB_FINAL_ERROR,
    /**
     * 发送到kafka失败
     */
    SEND_TO_KAFKA_FINAL_ERROR,
    /**
     * 不支持的事件
     */
    UN_SUPPORT_EVENT_ERROR,
    /**
     * 资金不足
     */
    BALANCE_INSUFFICIENT,
    /**
     * 完成
     */
    COMPLETE,
    /**
     * 错误
     */
    ERROR,
    /**
     * 取消
     */
    CANCEL,

}
