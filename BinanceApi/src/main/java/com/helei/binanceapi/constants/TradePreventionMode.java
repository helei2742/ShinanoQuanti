
package com.helei.binanceapi.constants;

import lombok.Getter;

@Getter
public enum TradePreventionMode {
    EXPIRE_TAKER("EXPIRE_TAKER"),
    EXPIRE_MAKER("EXPIRE_MAKER"),
    EXPIRE_BOTH("EXPIRE_BOTH"),
    NONE("NONE");


    private final String describe;

    TradePreventionMode(String describe) {
        this.describe = describe;
    }

    @Override
    public String toString() {
        return describe;
    }
}

