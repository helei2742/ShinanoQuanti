package com.helei.dto.order;

import com.helei.constants.order.OrderType;
import com.helei.constants.order.TimeInForce;
import lombok.*;

import java.math.BigDecimal;

/**
 * 限价单
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LimitOrder extends BaseOrder {

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

    public LimitOrder() {
        super(OrderType.LIMIT);
    }
}
