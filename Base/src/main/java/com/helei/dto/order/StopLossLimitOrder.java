package com.helei.dto.order;

import com.helei.constants.order.TimeInForce;
import com.helei.constants.order.OrderType;
import lombok.*;

import java.math.BigDecimal;

/**
 * 限价止损单
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class StopLossLimitOrder extends BaseOrder {


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

    public StopLossLimitOrder() {
        super(OrderType.STOP_LIMIT);
    }
}
