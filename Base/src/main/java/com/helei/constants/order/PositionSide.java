package com.helei.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 持仓方向
 */
@Getter
public enum PositionSide {

    BOTH("BOTH"),
    LONG("LONG"),
    SHORT("SHORT"),
    ;

    public static final Map<String, PositionSide> STATUS_MAP = new HashMap<>();

    static {
        for (PositionSide status : PositionSide.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    PositionSide(String description) {
        this.description = description;
    }
}
