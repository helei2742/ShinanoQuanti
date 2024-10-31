package com.helei.binanceapi.dto.accountevent;

import com.helei.constants.PositionSide;
import com.helei.binanceapi.constants.AccountEventType;
import lombok .*;

import java.util.List;

/**
 * <p>账户资金仓位发生变化事件</p>
 * <p>
 * 账户更新事件的 event type 固定为 ACCOUNT_UPDATE
 * <p/>
 * <p>
 * 当账户信息有变动时，会推送此事件： 1.仅当账户信息有变动时(包括资金、仓位、保证金模式等发生变化)，才会推送此事件；
 * 2.订单状态变化没有引起账户和持仓变化的，不会推送此事件；
 * 3.position 信息：仅当symbol仓位有变动时推送。
 * </p>
 *
 * <p>
 * "FUNDING FEE" 引起的资金余额变化，仅推送简略事件：
 * 1.当用户某全仓持仓发生"FUNDING FEE"时，事件ACCOUNT_UPDATE将只会推送相关的用户资产余额信息B(仅推送FUNDING FEE 发生相关的资产余额信息)，而不会推送任何持仓信息P。
 * 2.当用户某逐仓仓持仓发生"FUNDING FEE"时，事件ACCOUNT_UPDATE将只会推送相关的用户资产余额信息B(仅推送"FUNDING FEE"所使用的资产余额信息)，和相关的持仓信息P(仅推送这笔"FUNDING FEE"发生所在的持仓信息)，其余持仓信息不会被推送。
 * </p>
 * <p>
 * 字段"m"代表了事件推出的原因，包含了以下可能类型:
 * 1.DEPOSIT
 * 2.WITHDRAW
 * 3.ORDER
 * 4.FUNDING_FEE
 * 5.WITHDRAW_REJECT
 * 6.ADJUSTMENT
 * 7.INSURANCE_CLEAR
 * 8.ADMIN_DEPOSIT
 * 9.ADMIN_WITHDRAW
 * 10.MARGIN_TRANSFER
 * 11.MARGIN_TYPE_CHANGE
 * 12.ASSET_TRANSFER
 * 13.OPTIONS_PREMIUM_FEE
 * 14.OPTIONS_SETTLE_PROFIT
 * 15.AUTO_EXCHANGE
 * 16.COIN_SWAP_DEPOSIT
 * 17.COIN_SWAP_WITHDRAW
 * </p>
 * <p>
 * 字段"bc"代表了钱包余额的改变量，即 balance change，但注意其不包含仓位盈亏及交易手续费。
 * </p>
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
        private String model;

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
