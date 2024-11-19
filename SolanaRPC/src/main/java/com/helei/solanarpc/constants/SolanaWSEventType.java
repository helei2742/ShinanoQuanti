package com.helei.solanarpc.constants;

public enum SolanaWSEventType {

    /**
     * 订阅与指定账户相关的日志消息，包含合约调用、转账等信息。
     */
    logsNotification,

    /**
     * 订阅某个账户的状态变化，包括余额或数据更新。
     */
    accountNotification,

    /**
     * 订阅某个程序（智能合约）相关的账户变化。例如，监控某个代币合约的所有账户。
     */
    programNotification,

    /**
     * 订阅新区块槽位的变化事件。
     */
    slotNotification,

    /**
     * 订阅区块插槽的更新事件，包括未确认、确认和根槽位更新
     */
    slotsUpdatesNotification,

    /**
     * 订阅验证者投票的变化事件。
     */
    voteNotification

    ;
}

