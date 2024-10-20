package com.helei.binanceapi.dto.order;

import com.helei.binanceapi.constants.TimeInForce;
import com.helei.binanceapi.constants.order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 限价单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LimitOrder extends BaseOrder {
    /**
     * 交易类型
     */
    private final OrderType type = OrderType.LIMIT;

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
}
