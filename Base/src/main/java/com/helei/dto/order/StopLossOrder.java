package com.helei.dto.order;

import com.helei.constants.order.OrderType;
import lombok.*;

import java.math.BigDecimal;


/**
 * 止损单
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class StopLossOrder extends BaseOrder {

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

    public StopLossOrder() {
        super(OrderType.STOP_MARKET);
    }
}
