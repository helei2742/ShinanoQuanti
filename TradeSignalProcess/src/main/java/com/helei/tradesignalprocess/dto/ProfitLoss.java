package com.helei.tradesignalprocess.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 盈亏比
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ProfitLoss {

    /**
     * 压力位1
     */
    private Double upPrice;

    /**
     * 支撑位1
     */
    private Double downPrice;

    /**
     * 买入盈亏比
     * @param price price
     * @return Double
     */
    public Double buyRatio(double price) {

        return (upPrice - price) / (price - downPrice);
    }

    /**
     * 卖出盈亏比
     * @param price price
     * @return Double
     */
    public Double saleRatio(double price) {
        return (price - downPrice)/(upPrice - price);
    }
}
