package com.helei.dto.account;

import com.helei.constants.order.OrderType;
import com.helei.constants.order.PositionSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 账户仓位设置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountPositionConfig implements Serializable {

    /**
     * 订单类型
     */
    private OrderType orderType = OrderType.LIMIT;

    /**
     * 持仓方向
     */
    private PositionSide positionSide = PositionSide.BOTH;

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
