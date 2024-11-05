package com.helei.dto.order;

import com.helei.constants.order.OrderType;
import lombok.*;

import java.math.BigDecimal;

/**
 * 市价单
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MarketOrder extends BaseOrder {


    /**
     * 量
     */
    private BigDecimal quantity;

    /**
     *
     */
    private BigDecimal quoteOrderQty;

    public MarketOrder() {
        super(OrderType.MARKET);
    }
}
