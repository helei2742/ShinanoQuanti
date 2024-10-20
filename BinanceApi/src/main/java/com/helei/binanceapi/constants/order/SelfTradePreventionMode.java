package com.helei.binanceapi.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 防止自成交模式
 */
@Getter
public enum SelfTradePreventionMode {
    NONE("NONE"),
    EXPIRE_TAKER("EXPIRE_TAKER"),
    EXPIRE_BOTH("EXPIRE_BOTH"),
    EXPIRE_MAKER("EXPIRE_MAKER"),
    ;

    public static final Map<String, SelfTradePreventionMode> STATUS_MAP = new HashMap<>();

    static {
        for (SelfTradePreventionMode status : SelfTradePreventionMode.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    SelfTradePreventionMode(String description) {
        this.description = description;
    }

}
