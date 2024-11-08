package com.helei.dto.account;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccountTradeDetail implements Serializable {


    /**
     * 手续费等级
     */
    private int feeTier;

    /**
     * 是否可以交易
     */
    private boolean canTrade;

    /**
     *  是否可以入金
     */
    private boolean canDeposit;

    /**
     * 是否可以出金
     */
    private boolean canWithdraw;

    /**
     * 保留字段，请忽略
     */
    private int updateTime;

    /**
     * 是否是多资产模式
     */
    private boolean multiAssetsMargin;


    private int tradeGroupId;

    /**
     * 当前所需起始保证金总额(存在逐仓请忽略), 仅计算usdt资产
     */
    private BigDecimal totalInitialMargin;

    /**
     * 维持保证金总额, 仅计算usdt资产
     */
    private BigDecimal totalMaintMargin;

    /**
     * 账户总余额, 仅计算usdt资产
     */
    private BigDecimal totalWalletBalance;

    /**
     * 持仓未实现盈亏总额, 仅计算usdt资产
     */
    private BigDecimal totalUnrealizedProfit;

    /**
     * 保证金总余额, 仅计算usdt资产
     */
    private BigDecimal totalMarginBalance;

    /**
     * 持仓所需起始保证金(基于最新标记价格), 仅计算usdt资产
     */
    private BigDecimal totalPositionInitialMargin;

    /**
     * 当前挂单所需起始保证金(基于最新标记价格), 仅计算usdt资产
     */
    private BigDecimal totalOpenOrderInitialMargin;

    /**
     * 全仓账户余额, 仅计算usdt资产
     */
    private BigDecimal totalCrossWalletBalance;

    /**
     * 全仓持仓未实现盈亏总额, 仅计算usdt资产
     */
    private BigDecimal totalCrossUnPnl;

    /**
     * 可用余额, 仅计算usdt资产
     */
    private BigDecimal availableBalance;

    /**
     * 最大可转出余额, 仅计算usdt资产
     */
    private BigDecimal maxWithdrawAmount;
}


