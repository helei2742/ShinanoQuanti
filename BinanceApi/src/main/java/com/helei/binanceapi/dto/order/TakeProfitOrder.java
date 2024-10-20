package com.helei.binanceapi.dto.order;

        import com.helei.binanceapi.constants.order.OrderType;
        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
        import lombok.NoArgsConstructor;

        import java.math.BigDecimal;

/**
 * 止盈单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TakeProfitOrder extends BaseOrder {
    /**
     * 交易类型
     */
    private final OrderType type = OrderType.TAKE_PROFIT_MARKET;

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

