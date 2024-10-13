

package com.helei.tradedatacenter.entity;

import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.indicator.Indicator;
import lombok.*;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 8888L; // 显式声明 serialVersionUID

    public  static final KLine HISTORY_END_KLINE = KLine.builder().symbol("HISTORY_END_KLINE").build();

    public  static final KLine STREAM_END_KLINE = KLine.builder().symbol("STREAM_END_KLINE").build();

    /**
     * symbol
     */
    private String symbol;

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
     * k线频率
     */
    private KLineInterval kLineInterval;

    /**
     * 存放各种指标以及他的值
     */
    private HashMap<String, Indicator> indicators = new HashMap<>();

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
