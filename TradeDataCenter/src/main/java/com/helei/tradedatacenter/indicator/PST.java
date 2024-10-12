

package com.helei.tradedatacenter.indicator;

import com.helei.tradedatacenter.dto.TrendLine;
import com.helei.tradedatacenter.entity.KLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 支撑、压力、趋势线
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PST implements Indicator {

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


