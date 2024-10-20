package com.helei.binanceapi.constants;


import java.util.HashMap;
import java.util.Map;

/**
 * 策略状态
 */
public enum StrategyStatus {
    NEW("NEW"),
    WORKING("WORKING"),
    CANCELLED("CANCELLED"),
    EXPIRED("EXPIRED"),
    ;

    public static final Map<String, StrategyStatus> STATUS_MAP = new HashMap<>();

    static {
        for (StrategyStatus status : StrategyStatus.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    StrategyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
