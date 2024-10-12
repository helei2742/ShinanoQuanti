

package com.helei.tradedatacenter.indicator;

import com.helei.tradedatacenter.entity.KLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 支撑、压力、趋势线
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
     * 上升趋势线
     */
    private TrendLine upTrendLine;

    /**
     * 下降趋势线
     */
    private TrendLine downTrendLine;

    /**
     * 当前趋势是否向上
     */
    private boolean currentTrendUp;

    @Override
    public Indicator clone() {
        return new PST(pressure, support, upTrendLine, downTrendLine, currentTrendUp);
    }

    @Data
    public static class TrendLine {
        private double k;
        private double m;

        public TrendLine(double k, double m) {
            this.k = k;
            this.m = m;
        }

        public static TrendLine calculateTrend(List<KLine> data) {
            int n = data.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            for (KLine kLine : data) {
                double y = kLine.getClose();
                int x = kLine.getOpenTime().getSecond();

                sumY += y;
                sumX += x;
                sumXY += x * y;
                sumX2 += x * x;
            }

            double k = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double m = (sumY - k * sumX) / n;

            return  new TrendLine(k, m);
        }
    }
}


