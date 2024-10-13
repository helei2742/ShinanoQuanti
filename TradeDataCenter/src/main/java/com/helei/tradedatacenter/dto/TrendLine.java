package com.helei.tradedatacenter.dto;

import com.helei.tradedatacenter.entity.KLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendLine {
    private double k;
    private double m;

    public double predictPrice(LocalDateTime dateTime) {
        return k*dateTime.toInstant(ZoneOffset.UTC).getEpochSecond() + m;
    }

    public static TrendLine calculateTrend(List<KLine> data, Function<KLine, Double> getY) {
        if (data.isEmpty() || data.size() < 2) {
            return new TrendLine(0,0);
        }
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (KLine kLine : data) {
            double y =  getY.apply(kLine);
            long x = kLine.getOpenTime().toInstant(ZoneOffset.UTC).getEpochSecond();

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
