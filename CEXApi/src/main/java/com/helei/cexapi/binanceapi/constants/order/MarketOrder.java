package com.helei.cexapi.binanceapi.constants.order;

        import com.helei.cexapi.binanceapi.constants.order.TradeType;
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
    private final TradeType type = TradeType.MARKET;

    /**
     * 量
     */
    private BigDecimal quantity;

    /**
     *
     */
    private BigDecimal quoteOrderQty;
}
