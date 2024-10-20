package com.helei.binanceapi.dto.order;

        import com.helei.binanceapi.constants.order.OrderType;
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
    private final OrderType type = OrderType.STOP_MARKET;
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
