package com.helei.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件价格触发类型 (workingType)
 */
@Getter
public enum WorkingType {
    MARK_PRICE("MARK_PRICE"),
    CONTRACT_PRICE("CONTRACT_PRICE")
    ;

    public static final Map<String, WorkingType> STATUS_MAP = new HashMap<>();

    static {
        for (WorkingType status : WorkingType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    WorkingType(String description) {
        this.description = description;
    }
}
