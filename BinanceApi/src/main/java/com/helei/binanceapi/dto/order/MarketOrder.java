package com.helei.binanceapi.dto.order;

import com.helei.binanceapi.constants.order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 市价单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MarketOrder extends BaseOrder {

    /**
     * 交易类型
     */
    private final OrderType type = OrderType.MARKET;

    /**
     * 量
     */
    private BigDecimal quantity;

    /**
     *
     */
    private BigDecimal quoteOrderQty;
}
