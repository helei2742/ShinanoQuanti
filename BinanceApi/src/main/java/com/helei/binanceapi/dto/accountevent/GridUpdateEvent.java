package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import com.helei.binanceapi.constants.strategy.StrategyStatus;
import com.helei.binanceapi.constants.strategy.StrategyType;
import lombok.*;

/**
 * 网格更新推送事件
 */
@Setter
@Getter
@ToString

public class GridUpdateEvent extends AccountEvent {

    /**
     * 撮合时间 "T"
     */
    private Long matchMakingTime;

    /**
     * 策略更新信息 "gu"
     */
    private GridUpdateInfo gridUpdateInfo;

    public GridUpdateEvent(Long eventTime) {
        super(AccountEventType.GRID_UPDATE, eventTime);
    }



    // 内部类，用于表示 "gu" 部分的策略更新信息
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class GridUpdateInfo {
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
         * 已实现 PNL "r"
         */
        private Double realizedPnl;
        /**
         *  未配对均价 "up"
         */
        private Double unpairedAverage;
        /**
         * 未配对数量 "uq"
         */
        private Double unpairedQuantity;
        /**
         * 未配对手续费 "uf"
         */
        private Double unpairedFee;
        /**
         * 已配对 PNL "mp"
         */
        private Double matchedPnl;
        /**
         * 更新时间 "ut"
         */
        private Long updateTime;
    }
}
