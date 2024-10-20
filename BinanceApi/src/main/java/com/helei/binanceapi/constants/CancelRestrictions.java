package com.helei.binanceapi.constants;


/**
 * 根据订单状态撤销订单
 */
public enum CancelRestrictions {
    /**
     *  如果订单状态为 NEW，撤销将成功。
     */
    ONLY_NEW("ONLY_NEW "),

    /**
     * 如果订单状态为 PARTIALLY_FILLED，撤销将成功。
     */
    ONLY_PARTIALLY_FILLED("ONLY_PARTIALLY_FILLED "),
    ;

    private final String describe;

    CancelRestrictions(String describe) {
        this.describe = describe;
    }

    @Override
    public String toString() {
        return describe;
    }
}

