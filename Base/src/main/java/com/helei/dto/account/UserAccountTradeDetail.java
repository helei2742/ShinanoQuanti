package com.helei.dto.account;


import com.alibaba.fastjson.JSONObject;
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
    private Integer feeTier;

    /**
     * 是否可以交易
     */
    private Boolean canTrade;

    /**
     *  是否可以入金
     */
    private Boolean canDeposit;

    /**
     * 是否可以出金
     */
    private Boolean canWithdraw;

    /**
     * 保留字段，请忽略
     */
    private Integer updateTime;

    /**
     * 是否是多资产模式
     */
    private Boolean multiAssetsMargin;


    private Integer tradeGroupId;

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


    public static UserAccountTradeDetail generateFromJSON(JSONObject jb) {

        return UserAccountTradeDetail
                .builder()
                .feeTier(jb.getInteger("feeTier"))
                .canTrade(jb.getBoolean("canTrade"))
                .canDeposit(jb.getBoolean("canDeposit"))
                .canWithdraw(jb.getBoolean("canWithdraw"))
                .updateTime(jb.getInteger("updateTime"))
                .multiAssetsMargin(jb.getBoolean("multiAssetsMargin"))
                .tradeGroupId(jb.getInteger("tradeGroupId"))
                .totalInitialMargin(jb.getBigDecimal("totalInitialMargin"))
                .totalMaintMargin(jb.getBigDecimal("totalMaintMargin"))
                .totalWalletBalance(jb.getBigDecimal("totalWalletBalance"))
                .totalUnrealizedProfit(jb.getBigDecimal("totalUnrealizedProfit"))
                .totalMarginBalance(jb.getBigDecimal("totalMarginBalance"))
                .totalPositionInitialMargin(jb.getBigDecimal("totalPositionInitialMargin"))
                .totalOpenOrderInitialMargin(jb.getBigDecimal("totalOpenOrderInitialMargin"))
                .totalCrossWalletBalance(jb.getBigDecimal("totalCrossWalletBalance"))
                .totalCrossUnPnl(jb.getBigDecimal("totalCrossUnPnl"))
                .availableBalance(jb.getBigDecimal("availableBalance"))
                .maxWithdrawAmount(jb.getBigDecimal("maxWithdrawAmount"))
                .build();
    }
}


