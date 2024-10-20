package com.helei.binanceapi.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态
 */
@Getter
public enum OrderStatus {
    NEW("NEW"),
    PARTIALLY_FILLED("PARTIALLY_FILLED"),
    FILLED("FILLED"),
    CANCELED("CANCELED"),
    EXPIRED("EXPIRED")
    ;

    public static final Map<String, OrderStatus> STATUS_MAP = new HashMap<>();

    static {
        for (OrderStatus status : OrderStatus.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
