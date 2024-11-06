package com.helei.constants.order;



/**
 * 可选的响应格式: ACK，RESULT，FULL.
 * MARKET和LIMIT订单默认使用FULL，其他订单类型默认使用ACK。
 */
public enum OrderRespType {
    ACK,
    RESULT,
    FULL
}
