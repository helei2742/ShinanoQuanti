package com.helei.constants.order;

public enum GroupOrderStatus {

    /**
     * 自定义状态，订单被创建
     */
    CREATED,

    /**
     * 自定义状态，写入到kafka
     */
    WRITE_IN_KAFKA,

    /**
     * 自定义状态，写入到DB
     */
    WRITE_IN_DB,

    /**
     * 资金不足，不能开单
     */
    BALANCE_INSUFFICIENT,
    /**
     * 自定义状态，发送到交易所
     */
    SEND_TO_CEX

}
