package com.helei.binanceapi.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum AccountEventType {

    /**
     * listenKey过期推送
     * <p>
     * 事件描述
     * <p>
     * 当前连接使用的有效listenKey过期时，user data stream 将会推送此事件。
     * <p>
     * 注意:
     * <p>
     * 此事件与 websocket 连接中断没有必然联系
     * 只有正在连接中的有效listenKey过期时才会收到此消息
     * 收到此消息后 user data stream 将不再更新，直到用户使用新的有效的listenKey
     */
    LISTEN_KEY_EXPIRED("listenKeyExpired"),

    /**
     * Balance 和 Position 更新推送
     * <p>
     * 事件描述
     * <p>
     * 账户更新事件的 event type 固定为 ACCOUNT_UPDATE
     * 当账户信息有变动时，会推送此事件：
     * 仅当账户信息有变动时(包括资金、仓位、保证金模式等发生变化)，才会推送此事件；
     * 订单状态变化没有引起账户和持仓变化的，不会推送此事件；
     * position 信息：仅当 symbol 仓位有变动时推送。
     * "FUNDING FEE" 引起的资金余额变化，仅推送简略事件：
     * <p>
     * 当用户某全仓持仓发生"FUNDING FEE"时，事件ACCOUNT_UPDATE将只会推送相关的用户资产余额信息B
     * (仅推送 FUNDING FEE 发生相关的资产余额信息)，而不会推送任何持仓信息P。
     * 当用户某逐仓仓持仓发生"FUNDING FEE"时，事件ACCOUNT_UPDATE将只会推送相关的用户资产余额信息B
     * (仅推送"FUNDING FEE"所使用的资产余额信息)，和相关的持仓信息P(仅推送这笔"FUNDING FEE"发生所在的持仓信息)，其余持仓信息不会被推送。
     */
    ACCOUNT_UPDATE("ACCOUNT_UPDATE"),

    /**
     * 追加保证金通知
     * <p>
     * 事件描述
     * <p>
     * 当用户持仓风险过高，会推送此消息。
     * 此消息仅作为风险指导信息，不建议用于投资策略。
     * 在大波动市场行情下，不排除此消息发出的同时用户仓位已被强平的可能。
     */
    MARGIN_CALL("MARGIN_CALL"),

    /**
     * 订单交易更新推送
     * <p>
     * 事件描述
     * <p>
     * 当有新订单创建、订单有新成交或者新的状态变化时会推送此类事件 事件类型统一为 ORDER_TRADE_UPDATE
     */
    ORDER_TRADE_UPDATE("ORDER_TRADE_UPDATE"),

    /**
     * 精简交易推送
     * <p>
     * 相比原有的ORDER_TRADE_UPDATE流减少了数据延迟，但该交易推送仅推送和交易相关的字段
     */
    ORDER_TRADE_UPDATE_LITE("TRADE_LITE"),

    /**
     * 杠杆倍数等账户配置 更新推送
     * <p>
     * 事件描述
     * </p>
     * <p>
     * 当账户配置发生变化时会推送此类事件类型统一为ACCOUNT_CONFIG_UPDATE
     * 当交易对杠杆倍数发生变化时推送消息体会包含对象ac表示交易对账户配置，其中s代表具体的交易对，l代表杠杆倍数
     * 当用户联合保证金状态发生变化时推送消息体会包含对象ai表示用户账户配置，其中j代表用户联合保证金状态
     * </p>
     */
    ACCOUNT_CONFIG_UPDATE("ACCOUNT_CONFIG_UPDATE"),

    /**
     * 策略交易更新推送
     * <p>事件描述</p>
     * <p>STRATEGY_UPDATE 在策略交易创建、取消、失效等等时候更新。</p>
     */
    STRATEGY_UPDATE("STRATEGY_UPDATE"),
    ;

    public static final Map<String, AccountEventType> STATUS_MAP = new HashMap<>();

    static {
        for (AccountEventType status : AccountEventType.values()) {
            STATUS_MAP.put(status.getDescription(), status);
        }
    }

    private final String description;


    AccountEventType(String description) {
        this.description = description;
    }

}
