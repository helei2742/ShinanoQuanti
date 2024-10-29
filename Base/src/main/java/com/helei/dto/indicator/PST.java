package com.helei.dto.indicator;

import com.helei.dto.TrendLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 支撑、压力、趋势线
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PST implements Indicator, Serializable {

    @Serial
    private static final long serialVersionUID = -1451638498435687779L; // 显式声明 serialVersionUID


    /**
     * 压力线
     */
    private List<Double> pressure;

    /**
     * 支撑线
     */
    private List<Double> support;

    /**
     * 上趋势线,根据相对高点计算
     */
    private TrendLine relativeUpTrendLine;

    /**
     * 下趋势线，根据相对低点计算
     */
    private TrendLine relativeDownTrendLine;

    /**
     * 最大值
     */
    private double maxPrice;

    /**
     * 最小值
     */
    private double minPrice;

    @Override
    public Indicator clone() {
        return new PST(pressure, support, relativeUpTrendLine, relativeDownTrendLine, maxPrice, minPrice);
    }

}
