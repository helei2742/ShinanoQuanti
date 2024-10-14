package com.helei.cexapi.binanceapi.constants.order;

        import com.helei.cexapi.binanceapi.constants.order.TimeInForce;
        import com.helei.cexapi.binanceapi.constants.order.TradeType;
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
    private final TradeType type = TradeType.LIMIT;

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
