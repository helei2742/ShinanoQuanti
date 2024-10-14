

package com.helei.cexapi.binanceapi.constants.order;

public enum TradeSide {
    BUY("BUY"),
    SALE("SALE")
    ;

    private final String describe;

    TradeSide(String describe) {
        this.describe = describe;
    }


    @Override
    public String toString() {
        return describe;
    }
}