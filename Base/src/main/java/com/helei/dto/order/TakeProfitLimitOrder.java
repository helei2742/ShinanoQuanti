package com.helei.dto.order;

import com.helei.constants.order.OrderType;
import com.helei.constants.order.TimeInForce;
import lombok.*;

import java.math.BigDecimal;


/**
 * 限价止盈单
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TakeProfitLimitOrder extends BaseOrder {


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

    public TakeProfitLimitOrder() {
        super(OrderType.TAKE_PROFIT_LIMIT);
    }
}

