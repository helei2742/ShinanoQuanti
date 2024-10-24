package com.helei.dto;

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
    private KLine kLine;

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
        return kLine.getSymbol();
    }


    @Override
    public String toString() {
        return "TradeSignal{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", kLine=" + kLine +
                ", createTime=" + (createTime == null ? "null" : Instant.ofEpochMilli(createTime)) +
                ", tradeSide=" + tradeSide +
                ", currentPrice=" + currentPrice +
                ", targetPrice=" + targetPrice +
                ", stopPrice=" + stopPrice +
                ", isExpire=" + isExpire +
                '}';
    }
}
