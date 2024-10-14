package com.helei.cexapi.binanceapi.constants.order;

        import com.helei.cexapi.binanceapi.constants.order.TradeType;
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
    private final TradeType type = TradeType.TAKE_PROFIT;

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

