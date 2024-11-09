
package com.helei.binanceapi.constants.command;

import com.helei.constants.WSCommandType;

public enum AccountCommandType implements WSCommandType {

    /**
     * 创建一个新的user data stream，返回值为一个listenKey，即websocket订阅的stream名称。
     * 如果该帐户具有有效的listenKey，则将返回该listenKey并将其有效期延长60分钟。
     */
    USER_DATA_STREAM_START("userDataStream.start"),

    /**
     * Websocket API延长listenKey有效期
     * 有效期延长至本次调用后60分钟
     */
    USER_DATA_STREAM_PING("userDataStream.ping"),

    /**
     * Websocket API关闭listenKey
     * 关闭某账户数据流
     */
    USER_DATA_STREAM_CLOSE("userDataStream.stop"),

    /**
     * 账户信息
     */
    ACCOUNT_STATUS("account.status"),

    /**
     * 获取账户信息，只有u本位合约时，用于获取非0资产
     */
    ACCOUNT_STATUS_V2("v2/account.status"),

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
    MY_TRADES("myTrades"),

    /**
     * 账户余额
     */
    ACCOUNT_BALANCE("v2/account.balance")
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
