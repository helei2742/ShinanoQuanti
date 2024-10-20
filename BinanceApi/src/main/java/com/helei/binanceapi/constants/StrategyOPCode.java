package com.helei.binanceapi.constants;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 策略操作代码
 */
@Getter
public enum StrategyOPCode {
    /**
     * 策略参数更改
     */
    STRATEGY_PARAMS_CHANGE(8001, "Customize Toolbar..."),
    /**
     * 用户取消策略
     */
    STRATEGY_CANCEL(8002, "用户取消策略"),
    /**
     * 用户手动新增或取消订单
     */
    STRATEGY_USER_ADD_CANCEL_ORDER(8003, " 用户手动新增或取消订单"),
    /**
     * 达到 stop limit
     */
    ARRIVE_STOP_LIMIT(8004, "达到 stop limit"),
    /**
     * 用户仓位爆仓
     */
    USER_POSITION_CANCEL(8005, "用户仓位爆仓"),
    /**
     * 已达最大可挂单数量
     */
    ORDER_LIMIT(8006, "已达最大可挂单数量"),
    /**
     * 新增网格策略
     */
    NEW_GRID_STRATEGY(8007, "新增网格策略"),
    /**
     *  保证金不足
     */
    BAIL_INSUFFICIENT(8008, "保证金不足"),
    /**
     * 价格超出范围
     */
    PRICE_OUT_LIMIT(8009, "价格超出范围"),
    /**
     * 市场非交易状态
     */
    MARKET_TRADE_CLOSE(8010, "市场非交易状态"),
    /**
     * 关仓失败，平仓单无法成交
     */
    POSITION_CLOSE_ERROR(8011, "关仓失败，平仓单无法成交"),
    /**
     * 超过最大可交易名目金额
     */
    DAILY_TRADE_COUNT_OUT_LIMIT(8012, "超过最大可交易名目金额"),
    /**
     * 不符合网格交易身份
     */
    GAIL_LIMIT(8013, "不符合网格交易身份"),
    /**
     * 不符合 Futures Trading Quantitative Rules，策略终止
     */
    INCONFORMITY_Futures_Trading_Quantitative_Rules(8014, "不符合 Futures Trading Quantitative Rules，策略终止"),
    /**
     * 无仓位或是仓位已经爆仓
     */
    NO_POSITION(8015, "无仓位或是仓位已经爆仓")
    ;

    public static final Map<Integer, StrategyOPCode> STATUS_MAP = new HashMap<>();

    static {
        for (StrategyOPCode status : StrategyOPCode.values()) {
            STATUS_MAP.put(status.getCode(), status);
        }
    }

    private final Integer code;

    private final String description;

    StrategyOPCode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
