package com.helei.binanceapi.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 盘口价下单模式:
 */
@Getter
public enum OpponentPriceMode {
    /**
     *  (盘口对手价)
     */
    OPPONENT("OPPONENT"),
    /**
     * (盘口对手5档价)
     */
    OPPONENT_5("OPPONENT_5"),
    /**
     * (盘口对手10档价)
     */
    OPPONENT_10("OPPONENT_10"),
    /**
     * (盘口对手20档价)
     */
    OPPONENT_20("OPPONENT_20"),
    /**
     * (盘口同向价)
     */
    QUEUE("QUEUE"),
    /**
     * (盘口同向排队5档价)
     */
    QUEUE_5("QUEUE_5"),
    /**
     *  (盘口同向排队10档价)
     */
    QUEUE_10("QUEUE_10"),
    /**
     * (盘口同向排队20档价)
     */
    QUEUE_20("QUEUE_20"),
    ;

    public static final Map<String, OpponentPriceMode> STATUS_MAP = new HashMap<>();

    static {
        for (OpponentPriceMode status : OpponentPriceMode.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    OpponentPriceMode(String description) {
        this.description = description;
    }

}
