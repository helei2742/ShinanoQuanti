

package com.helei.cexapi.binanceapi.constants.order;

        import lombok.Getter;

@Getter
public enum TradeType {

    /**
     * 限价单
     */
    LIMIT("LIMIT"),
    /**
     * 市价单
     */
    MARKET("MARKET"),
    /**
     *  止损单
     */
    STOP_LOSS("STOP_LOSS"),
    /**
     * 限价止损单
     */
    STOP_LOSS_LIMIT("STOP_LOSS_LIMIT"),
    /**
     * 止盈单
     */
    TAKE_PROFIT("TAKE_PROFIT"),
    /**
     * 限价止盈单
     */
    TAKE_PROFIT_LIMIT("TAKE_PROFIT_LIMIT"),
    /**
     *  限价只挂单
     */
    LIMIT_MAKER("LIMIT_MAKER"),
    ;


    private final String describe;

    TradeType(String describe) {
        this.describe = describe;
    }

    @Override
    public String toString() {
        return describe;
    }
}