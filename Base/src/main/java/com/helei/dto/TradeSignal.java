package com.helei.dto;

import com.helei.constants.KLineInterval;
import com.helei.constants.TradeSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


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
     * 信号描述
     */
    private String description;

    /**
     * 发出这个信号的k线
     */
    private String symbol;

    private String interval;

    private boolean isEnd;

    private long closeTime;


    /**
     * 当前时间
     */
    private Long createTime;

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
    private Double stopPrice;

    /**
     * 信号是否过期
     */
    private Boolean isExpire;

    /**
     * 获取信号流的名字
     *
     * @return streamName
     */
    public String getStreamKey() {
        return symbol;
    }


    public String getKlineStreamKey() {
        return KLine.getKLineStreamKey(symbol, interval);
    }
}
