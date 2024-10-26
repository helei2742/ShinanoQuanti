package com.helei.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 中心化交易所类型
 */
@Getter
public enum CEXType {
    BINANCE("BINANCE"),
    ;

    public static final Map<String, CEXType> STATUS_MAP = new HashMap<>();

    static {
        for (CEXType status : CEXType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    CEXType(String description) {
        this.description = description;
    }

}
