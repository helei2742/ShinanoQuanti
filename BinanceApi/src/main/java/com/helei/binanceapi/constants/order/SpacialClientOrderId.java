package com.helei.binanceapi.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 特殊的自定义订单ID:
 */
@Getter
public enum SpacialClientOrderId {
    /**
     * 系统强平订单
     */
    AUTO_CLOSE("autoclose"),
    /**
     * ADL自动减仓订单
     */
    ADL_AUTP_CLOSE("adl_autoclose"),
    /**
     * 下架或交割的
     */
    SETTLEMENT_AUTP_CLOSE("settlement_autoclose")
    ;

    public static final Map<String, SpacialClientOrderId> STATUS_MAP = new HashMap<>();

    static {
        for (SpacialClientOrderId status : SpacialClientOrderId.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;

    SpacialClientOrderId(String description) {
        this.description = description;
    }

}
