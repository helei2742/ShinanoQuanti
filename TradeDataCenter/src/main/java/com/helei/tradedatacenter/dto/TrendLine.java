package com.helei.tradedatacenter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

/**
 * 趋势线，本质是一根直线。k m 为点斜式的斜率和偏移
 * TrendLine.calculateTrend() 采用最小二乘法计算趋势线
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendLine {
    private double k;
    private double m;

    public double predictPrice(LocalDateTime dateTime) {
        return k*dateTime.toInstant(ZoneOffset.UTC).getEpochSecond() + m;
    }
}
