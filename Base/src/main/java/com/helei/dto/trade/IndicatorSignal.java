package com.helei.dto.trade;

import com.helei.constants.trade.TradeSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * 指标信号
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndicatorSignal {

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
        return kLine.getStreamKey();
    }


    public String getKlineStreamKey() {
        return kLine.getStreamKey();
    }
}
