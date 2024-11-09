package com.helei.binanceapi.dto.accountevent;

import com.helei.constants.order.PositionSide;
import com.helei.constants.trade.MarginMode;
import com.helei.binanceapi.constants.AccountEventType;
import lombok.*;

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
    private BPUpdateReason reason;

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
        private Double entryPrice;

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
         */
        private MarginMode marginMode;

        /**
         * 保证金
         */
        private Double bail;

        /**
         * 持仓方向
         */
        private PositionSide positionSide;

    }


    /**
     * 资金仓位更新事件的原因
     */
    public enum BPUpdateReason {
        /**
         * 存款 - 资金存入账户。
         */
        DEPOSIT,

        /**
         * 提款 - 从账户中提取资金。
         */
        WITHDRAW,

        /**
         * 订单 - 由于交易订单引起的余额变化。
         */
        ORDER,

        /**
         * 资金费用 - 由于杠杆或融资费用引起的余额变化。
         */
        FUNDING_FEE,

        /**
         * 提现拒绝 - 提现请求被拒绝后资金返还到账户。
         */
        WITHDRAW_REJECT,

        /**
         * 调整 - 管理员手动调整账户余额。
         */
        ADJUSTMENT,

        /**
         * 保险清算 - 清算账户或保险相关的余额调整。
         */
        INSURANCE_CLEAR,

        /**
         * 管理员存款 - 管理员手动向账户存入资金。
         */
        ADMIN_DEPOSIT,

        /**
         * 管理员提款 - 管理员手动从账户提款。
         */
        ADMIN_WITHDRAW,

        /**
         * 保证金转移 - 保证金账户之间的资金转移。
         */
        MARGIN_TRANSFER,

        /**
         * 保证金类型更改 - 保证金类型更改导致的余额调整。
         */
        MARGIN_TYPE_CHANGE,

        /**
         * 资产转移 - 账户内外的资产转移。
         */
        ASSET_TRANSFER,

        /**
         * 期权权利金费用 - 由于期权权利金导致的费用扣除。
         */
        OPTIONS_PREMIUM_FEE,

        /**
         * 期权结算收益 - 期权交易结算带来的收益。
         */
        OPTIONS_SETTLE_PROFIT,

        /**
         * 自动兑换 - 由于自动兑换货币导致的余额变化。
         */
        AUTO_EXCHANGE,

        /**
         * 币币兑换存款 - 币币兑换后的存款。
         */
        COIN_SWAP_DEPOSIT,

        /**
         * 币币兑换提款 - 币币兑换后的提款。
         */
        COIN_SWAP_WITHDRAW
    }
}
