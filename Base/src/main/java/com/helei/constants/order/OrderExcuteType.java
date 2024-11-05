package com.helei.constants.order;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单推送事件的具体执行类型
 */
@Getter
public enum OrderExcuteType {

    NEW("NEW"),

    /**
     * 已撤
     */
    CANCELED("CANCELED"),

    /**
     * 订单 ADL 或爆仓
     */
    CALCULATED("CALCULATED"),

    /**
     * 订单失效
     */
    EXPIRED("EXPIRED"),

    /**
     * 交易
     */
    TRADE("TRADE"),

    /**
     * 订单修改
     */
    AMENDMENT("AMENDMENT"),

    ;

    public static final Map<String, OrderExcuteType> STATUS_MAP = new HashMap<>();

    static {
        for (OrderExcuteType status : OrderExcuteType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    OrderExcuteType(String description) {
        this.description = description;
    }
}
