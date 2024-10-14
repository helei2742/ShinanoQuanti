
package com.helei.cexapi.binanceapi.constants.command;

public enum AccountCommandType implements WSCommandType{

    /**
     * 账户信息
     */
    ACCOUNT_STATUS("account.status"),

    /**
     * 查询未成交的订单计数
     */
    ACCOUNT_RATE_LIMITS_ORDERS("account.rateLimits.orders"),

    /**
     * 账户订单历史
     */
    ACCOUNT_ALL_ORDERS("account.allOrders"),

    /**
     * 账户成交历史
     */
    MY_TRADES("myTrades")
    ;

    AccountCommandType(String description) {
        this.description = description;
    }

    private final String description;

    @Override
    public String toString() {
        return description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}