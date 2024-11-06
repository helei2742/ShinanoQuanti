package com.helei.dto.trade;

import com.helei.constants.trade.TradeSide;
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
public class TradeSignal {

    /**
     * 信号id
     */
    private String id;

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

    /**
     * 信号创建时间戳
     */
    private long createTimestamp;

    /**
     * 信号创建的k线open时间
     */
    private long createKLineOpenTimestamp;
}

