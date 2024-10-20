package com.helei.binanceapi.dto.order;

import com.helei.binanceapi.constants.TimeInForce;
import com.helei.binanceapi.constants.order.OrderType;
import lombok.*;

import java.math.BigDecimal;

/**
 * 限价止损单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StopLossLimitOrder extends BaseOrder {

    /**
     * 交易类型
     */
    private final OrderType type = OrderType.STOP;

    /**
     * 有效成交方式
     */
    private TimeInForce timeInForce;
    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 量
     */
    private BigDecimal quantity;


    /**
     * 止损
     */
    private BigDecimal stopPrice;

    /**
     * 移动止损
     */
    private Integer trailingDelta;

}


