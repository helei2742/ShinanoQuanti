package com.helei.constants.order;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 防止自成交模式
 */
@Getter
public enum SelfTradePreventionMode {
    NONE,
    EXPIRE_TAKER,
    EXPIRE_BOTH,
    EXPIRE_MAKER,
}
