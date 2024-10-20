package com.helei.binanceapi.dto.order;

import com.helei.binanceapi.constants.TimeInForce;
import com.helei.binanceapi.constants.order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


/**
 * 限价止盈单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TakeProfitLimitOrder extends BaseOrder {
    /**
     * 交易类型
     */
    private final OrderType type = OrderType.TAKE_PROFIT_LIMIT;

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
