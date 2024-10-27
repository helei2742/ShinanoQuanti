package com.helei.dto;

import com.helei.constants.KLineInterval;
import com.helei.dto.indicator.Indicator;
import com.helei.dto.indicator.config.IndicatorConfig;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;

/**
 * K线实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class KLine implements Serializable {

    @Serial
    private static final long serialVersionUID = 8888L; // 显式声明 serialVersionUID

    public  static final KLine HISTORY_END_KLINE = KLine.builder().symbol("HISTORY_END_KLINE").build();

    public  static final KLine STREAM_END_KLINE = KLine.builder().symbol("STREAM_END_KLINE").build();

    /**
     * symbol
     */
    private String symbol = "";

    /**
     * 开盘价格
     */
    private double open;

    /**
     * 收盘价格
     */
    private double close;

    /**
     * 最高价格
     */
    private double high;

    /**
     * 最低价格
     */
    private double low;

    /**
     * 成交量
     */
    private double volume;

    /**
     * 开盘时间
     */
    private long openTime;

    /**
     * 收盘时间
     */
    private long closeTime;


    /**
     * 这根线是否执行完
     */
    private boolean end;

    /**
     * k线频率
     */
    private KLineInterval kLineInterval;

    /**
     * 存放各种指标以及他的值
     */
    private HashMap<IndicatorConfig<? extends Indicator>, Indicator> indicators = new HashMap<>();


    public <T extends Indicator> T getIndicator(IndicatorConfig<T> config) {
        Indicator indicator = indicators.get(config);
        if (indicator == null) return null;
        return (T) indicator;
    }

    /**
     * 获取stream流名称
     * @return stream流名称
     */
    public String getStreamKey() {
        if (kLineInterval == null) {
            System.out.println("---");
        }
        return symbol + "@kline_" + kLineInterval.getDescribe();
    }


    @Override
    public String toString() {
        return "KLine{" +
                "symbol='" + symbol + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                ", volume=" + volume +
                ", openTime=" + Instant.ofEpochMilli(openTime) +
                ", closeTime=" + Instant.ofEpochMilli(closeTime) +
                ", end=" + end +
                ", indicators=" + indicators +
                '}';
    }

    public KLine clone() {
        return KLine.builder().symbol(symbol).open(open).close(close).high(high).low(low).volume(volume).openTime(openTime).closeTime(closeTime).end(end).indicators(indicators).kLineInterval(kLineInterval).build();
    }
}

