package com.helei.cexapi.binanceapi.constants.order;

        import com.helei.cexapi.binanceapi.constants.order.TimeInForce;
        import com.helei.cexapi.binanceapi.constants.order.TradeType;
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
    private final TradeType type = TradeType.TAKE_PROFIT_LIMIT;

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
