package com.helei.tradedatacenter.dto;

import com.helei.cexapi.binanceapi.constants.order.TradeSide;
import lombok.*;

import java.math.BigDecimal;


/**
 * 原始订单数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class OriginOrder {

    /**
     * 系统中的订单id
     */
    private String orderId;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 交易方向
     */
    private TradeSide tradeSide;

    /**
     * 目标价格
     */
    private BigDecimal targetPrice;

    /**
     * 进场价格
     */
    private BigDecimal enterPrice;

    /**
     * 止损价格
     */
    private BigDecimal stopPrice;
}



