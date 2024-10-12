
package com.helei.tradedatacenter.entity;

import com.helei.tradedatacenter.constants.TradeSide;
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
