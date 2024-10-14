

package com.helei.cexapi.binanceapi.constants.command;

        import lombok.Getter;

@Getter
public enum TradeCommandType implements WSCommandType {

    /**
     * 下新订单
     */
    ORDER_PLACE("order.place"),
    /**
     * 测试下单
     */
    ORDER_TEST("order.test"),
    /**
     * 查询订单
     */
    ORDER_STATUS("order.status"),
    /**
     * 撤销订单
     */
    ORDER_CANCEL("order.cancel"),

    /**
     * 当前挂单
     */
    OPEN_ORDER_STATUS("open.order.status"),

    /**
     * 撤销单一交易对所有挂单
     */
    OPEN_ORDER_CANCEL_ALL("open.order.cancel.all"),
    ;

    TradeCommandType(String description) {
        this.description = description;
    }

    private final String description;

    @Override
    public String toString() {
        return description;
    }
}
