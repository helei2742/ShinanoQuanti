package com.helei.tradedatacenter.entity;

import com.helei.cexapi.binanceapi.constants.order.TradeSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 交易信号
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeSignal {

    /**
     * 信号名
     */
    private String name;

    /**
     * 发出这个信号的k线
     */
    private KLine kLine;

    /**
     * 当前时间
     */
    private LocalDateTime createTime;

    /**
     * 交易方向
     */
    private TradeSide tradeSide;

    /**
     * 当前价格
     */
    private Double currentPrice;

    /**
     * 目标价格
     */
    private Double targetPrice;

    /**
     * 止损价格
     */
    private Double protectPrice;
}

