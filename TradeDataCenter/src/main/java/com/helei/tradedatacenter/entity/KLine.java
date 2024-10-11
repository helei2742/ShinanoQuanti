

package com.helei.tradedatacenter.entity;

import com.helei.tradedatacenter.indicator.Indicator;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * K线实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class KLine {

    /**
     * symbol
     */
    private String symbol;

    /**
     * 开盘价格
     */
    private Double open;

    /**
     * 收盘价格
     */
    private Double close;

    /**
     * 最高价格
     */
    private Double high;

    /**
     * 最低价格
     */
    private Double low;

    /**
     * 成交量
     */
    private Double volume;

    /**
     * 开盘时间
     */
    private LocalDateTime openTime;

    /**
     * 收盘时间
     */
    private LocalDateTime closeTime;


    /**
     * 这根线是否执行完
     */
    private boolean end;

    /**
     * 存放各种指标以及他的值
     */
    private Map<String, Indicator> indicators = new HashMap<>();

    @Override
    public String toString() {
        return "KLine{" +
                "symbol='" + symbol + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                ", volume=" + volume +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                ", end=" + end +
                ", indicators=" + indicators +
                '}';
    }
}