package com.helei.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态
 */
@Getter
public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    EXPIRED,

    CREATED,
    WRITE_IN_DB,
    WRITE_IN_KAFKA,

    ;

}
