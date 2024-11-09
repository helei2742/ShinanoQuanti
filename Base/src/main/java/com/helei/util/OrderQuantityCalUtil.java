package com.helei.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算订单 量工具
 */
public class OrderQuantityCalUtil {


    /**
     * 根据风险承受百分比计算还能开的数量
     *
     * @param remainingCapital 剩余资金
     * @param riskPercentage   风险百分比
     * @param currentPrice     当前价格
     * @param entryPrice       已有仓位入场价
     * @param currentQuantity  已有仓位数量
     * @param stopPrice        止损价格
     * @return 能开的数量
     */
    public static BigDecimal riskPercentBasedQuantityCalculate(
            BigDecimal remainingCapital,
            BigDecimal riskPercentage,
            BigDecimal currentPrice,
            BigDecimal entryPrice,
            BigDecimal currentQuantity,
            BigDecimal stopPrice

    ) {
        BigDecimal existingRisk = entryPrice.subtract(stopPrice).multiply(currentQuantity);

        BigDecimal riskCapital = remainingCapital.multiply(riskPercentage);

        return  riskCapital.subtract(existingRisk).divide(currentPrice.subtract(stopPrice), RoundingMode.DOWN);
    }
}

