package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.order.PositionSide;
import com.helei.binanceapi.constants.AccountEventType;
import lombok.*;

import java.util.List;

/**
 * 账户资金仓位发生变化事件
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public class BalancePositionUpdateEvent extends AccountEvent {

    /**
     * 撮合时间
     */
    private Long matchMakingTime;

    /**
     * 事件推出原因
     */
    private String reason;

    /**
     * 资金发生变化的信息
     */
    private List<BalanceChangeInfo> balanceChangeInfos;

    /**
     * 仓位发生变化的信息
     */
    private List<PositionChangeInfo> positionChangeInfos;

    public BalancePositionUpdateEvent(Long eventTime) {
        super(AccountEventType.ACCOUNT_UPDATE, eventTime);
    }



    /**
     * 资产变化信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class BalanceChangeInfo {

        /**
         * 资产名称
         */
        private String asset;

        /**
         * 钱包余额
         */
        private Double walletBalance;

        /**
         * 除去逐仓保证金的钱包余额
         */
        private Double bailRemoveWalletBalance;

        /**
         * 除去盈亏和手续费的变化量
         */
        private Double walletBalanceChange;
    }


    /**
     * 仓位变化信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PositionChangeInfo {

        /**
         * 交易对
         */
        private String symbol;

        /**
         * 仓位
         */
        private Double position;

        /**
         * 入仓价格
         */
        private Double enterPosition;

        /**
         * 盈亏平衡价
         */
        private Double balanceEqualPrice;

        /**
         * 总盈亏
         */
        private Double countProfitOrLoss;

        /**
         * 未实现盈亏
         */
        private Double unrealizedProfitOrLoss;

        /**
         * 保证金模式
         * TODO 切换枚举
         */
        private String  model;

        /**
         * 保证金
         */
        private Double bail;

        /**
         * 持仓方向
         */
        private PositionSide positionSide;

    }

}
