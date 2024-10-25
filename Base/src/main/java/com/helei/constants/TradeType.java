package com.helei.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Getter
public enum TradeType {
    SPOT("SPOT"),
    CONTRACT("CONTRACT"),

    ;

    public static final Map<String, TradeType> STATUS_MAP = new HashMap<>();

    static {
        for (TradeType status : TradeType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    TradeType(String description) {
        this.description = description;
    }

}
