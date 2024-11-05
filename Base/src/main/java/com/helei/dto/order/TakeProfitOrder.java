package com.helei.dto.order;

import com.helei.constants.order.OrderType;
import lombok.*;

import java.math.BigDecimal;

/**
 * 止盈单
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TakeProfitOrder extends BaseOrder {


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

    public TakeProfitOrder() {
        super(OrderType.TAKE_PROFIT_MARKET);
    }
}

