package com.helei.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账户仓位设置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountLocationConfig {

    /**
     * 风险百分比
     */
    private double riskPercent;

    /**
     * 杠杠倍数
     */
    private int leverage;


    /**
     * 止损金额
     */
    private int stopLoss;

}
