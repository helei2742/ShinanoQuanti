package com.helei.tradedatacenter.dto;

import com.helei.tradedatacenter.entity.KLine;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class TrendLine {
    private double k;
    private double m;

    public TrendLine(double k, double m) {
        this.k = k;
        this.m = m;
    }

    public static TrendLine calculateTrend(List<KLine> data, Function<KLine, Double> getY) {
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (KLine kLine : data) {
            double y =  getY.apply(kLine);
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