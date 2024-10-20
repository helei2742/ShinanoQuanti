package com.helei.binanceapi.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 有效方式
 */
@Getter
public enum TimeInForce {
    /**
     * - Good Till Cancel 成交为止（下单后仅有1年有效期，1年后自动取消）
     */
    GTC("GTC"),
    /**
     * - Immediate or Cancel 无法立即成交(吃单)的部分就撤销
     */
    IOC("IOC"),
    /**
     * - Fill or Kill 无法全部立即成交就撤销
     */
    FOK("FOK"),
    /**
     * - Good Till Crossing 无法成为挂单方就撤销
     */
    GTX("GTX"),
    /**
     * - Good Till Date 在特定时间之前有效，到期自动撤销
     */
    GTD("GTD")

    ;


    public static final Map<String, TimeInForce> STATUS_MAP = new HashMap<>();

    static {
        for (TimeInForce status : TimeInForce.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;


    TimeInForce(String description) {
        this.description = description;
    }
}
