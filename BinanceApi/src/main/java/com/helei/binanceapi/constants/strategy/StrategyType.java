package com.helei.binanceapi.constants.strategy;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 策略类型
 */
@Getter
public enum StrategyType {
    /**
     * 网格
     */
    GRID("GRID"),
    ;

    public static final Map<String, StrategyType> STATUS_MAP = new HashMap<>();

    static {
        for (StrategyType status : StrategyType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    StrategyType(String description) {
        this.description = description;
    }

}
