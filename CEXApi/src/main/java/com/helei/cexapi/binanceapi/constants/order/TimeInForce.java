
package com.helei.cexapi.binanceapi.constants.order;

        import lombok.Getter;

/**
 * 有效成交方式
 */
@Getter
public enum TimeInForce {
    /**
     * 成交为止
     * 订单会一直有效，直到被成交或者取消。
     */
    GTC("GTC"),
    /**
     * 无法立即成交的部分就撤销
     * 订单在失效前会尽量多的成交。
     */
    IOC("IOC"),
    /**
     * 无法全部立即成交就撤销
     * 如果无法全部成交，订单会失效。
     */
    FOK("FOK"),

    ;


    private final String describe;

    TimeInForce(String describe) {
        this.describe = describe;
    }


    @Override
    public String toString() {
        return describe;
    }
}
