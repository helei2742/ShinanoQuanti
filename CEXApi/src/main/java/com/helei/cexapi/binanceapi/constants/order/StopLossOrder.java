package com.helei.cexapi.binanceapi.constants.order;

        import com.helei.cexapi.binanceapi.constants.order.TradeType;
        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
        import lombok.NoArgsConstructor;

        import java.math.BigDecimal;


/**
 * 止损单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StopLossOrder extends BaseOrder {
    /**
     * 交易类型
     */
    private final TradeType type = TradeType.STOP_LOSS;
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
