package com.helei.constants.order;

import lombok.Getter;


import java.util.HashMap;
import java.util.Map;

/**
 * 可选的响应格式: ACK，RESULT，FULL.
 * MARKET和LIMIT订单默认使用FULL，其他订单类型默认使用ACK。
 */
@Getter
public enum OrderRespType {
    ACK("ACK"),
    RESULT("RESULT"),
    FULL("FULL")
    ;

    public static final Map<String, OrderRespType> STATUS_MAP = new HashMap<>();

    static {
        for (OrderRespType status : OrderRespType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    OrderRespType(String description) {
        this.description = description;
    }

}
