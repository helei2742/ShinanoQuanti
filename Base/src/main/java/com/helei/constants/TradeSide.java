package com.helei.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易方向
 */
@Getter
public enum TradeSide {
    BUY("BUY"),
    SALE("SALE")
    ;

    public static final Map<String, TradeSide> STATUS_MAP = new HashMap<>();

    static {
        for (TradeSide status : TradeSide.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    TradeSide(String description) {
        this.description = description;
    }

}
