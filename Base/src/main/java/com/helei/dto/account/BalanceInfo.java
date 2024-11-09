package com.helei.dto.account;

import com.alibaba.fastjson.JSONObject;
import lombok.*;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceInfo {

    /**
     *  资产
     */
    private String asset;
    /**
     * 余额
     */
    private BigDecimal walletBalance;
    /**
     * 未实现盈亏
     */
    private BigDecimal unrealizedProfit;
    /**
     * 保证金余额
     */
    private BigDecimal marginBalance;
    /**
     * 维持保证金
     */
    private BigDecimal maintMargin;
    /**
     * 当前所需起始保证金
     */
    private BigDecimal initialMargin;
    /**
     *  持仓所需起始保证金
     */
    private BigDecimal positionInitialMargin;
    /**
     * 当前挂单所需起始保证金
     */
    private BigDecimal openOrderInitialMargin;
    /**
     * 全仓账户余额
     */
    private BigDecimal crossWalletBalance;
    /**
     * 全仓持仓未实现盈亏
     */
    private BigDecimal crossUnPnl;
    /**
     * 可用余额
     */
    private BigDecimal availableBalance;
    /**
     * 最大可转出余额
     */
    private BigDecimal maxWithdrawAmount;
    /**
     * 是否可用作联合保证金
     */
    private Boolean marginAvailable;
    /**
     * 更新时间
     */
    private Long updateTime;


    public static BalanceInfo fromJson(JSONObject jsonObject) {
        BalanceInfo assetBalance = new BalanceInfo();
        assetBalance.setAsset(jsonObject.getString("asset"));
        assetBalance.setWalletBalance(jsonObject.getBigDecimal("walletBalance"));
        assetBalance.setUnrealizedProfit(jsonObject.getBigDecimal("unrealizedProfit"));
        assetBalance.setMarginBalance(jsonObject.getBigDecimal("marginBalance"));
        assetBalance.setMaintMargin(jsonObject.getBigDecimal("maintMargin"));
        assetBalance.setInitialMargin(jsonObject.getBigDecimal("initialMargin"));
        assetBalance.setPositionInitialMargin(jsonObject.getBigDecimal("positionInitialMargin"));
        assetBalance.setOpenOrderInitialMargin(jsonObject.getBigDecimal("openOrderInitialMargin"));
        assetBalance.setCrossWalletBalance(jsonObject.getBigDecimal("crossWalletBalance"));
        assetBalance.setCrossUnPnl(jsonObject.getBigDecimal("crossUnPnl"));
        assetBalance.setAvailableBalance(jsonObject.getBigDecimal("availableBalance"));
        assetBalance.setMaxWithdrawAmount(jsonObject.getBigDecimal("maxWithdrawAmount"));
        assetBalance.setMarginAvailable(jsonObject.getBoolean("marginAvailable"));
        assetBalance.setUpdateTime(jsonObject.getLongValue("updateTime"));
        return assetBalance;
    }
}
