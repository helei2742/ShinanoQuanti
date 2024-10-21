package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import com.helei.binanceapi.constants.strategy.StrategyOPCode;
import com.helei.binanceapi.constants.strategy.StrategyStatus;
import com.helei.binanceapi.constants.strategy.StrategyType;
import lombok.*;


/**
 * 策略交易更新推送
 */
@Getter
@Setter
@ToString

public class StrategyUpdateEvent extends AccountEvent {
    /**
     * 撮合时间
     */
    private Long matchMakingTime;


    private StrategyUpdateInfo strategyUpdateInfo;

    public StrategyUpdateEvent(Long eventTime) {
        super(AccountEventType.STRATEGY_UPDATE, eventTime);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyUpdateInfo{
        /**
         * 策略 ID "si"
         */
        private Long strategyId;
        /**
         * 策略类型 "st"
         */
        private StrategyType strategyType;
        /**
         * 策略状态 "ss"
         */
        private StrategyStatus strategyStatus;
        /**
         * 交易对 "s"
         */
        private String symbol;
        /**
         * 更新时间 "ut"
         */
        private long updateTime;
        /**
         *  操作码 "c"
         */
        private StrategyOPCode strategyOPCode;
    }
}
