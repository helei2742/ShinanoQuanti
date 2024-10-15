


package com.helei.tradedatacenter.util;

import com.helei.tradedatacenter.dto.TrendLine;
import com.helei.tradedatacenter.entity.KLine;

import java.util.List;
import java.util.function.Function;

public class CalculatorUtil {


    /**
     *  EMA 计算公式
     * @param price 当前价格
     * @param previousMA 前一个ema值
     * @param period 间隔
     * @return 当前ma
     */
    public static double calculateMA(double price, double previousMA, int period) {
        return (previousMA * (period - 1) + price) / period;
    }


    /**
     *  EMA 计算公式
     * @param price 当前价格
     * @param previousEMA 前一个ema值
     * @param period 间隔
     * @return 当前ema
     */
    public static double calculateEMA(double price, double previousEMA, int period) {
        double alpha = 2.0 / (period + 1.0);
        return alpha * price + (1 - alpha) * previousEMA;
    }


    /**
     * 计算rsi
     * @param open 开盘价格
     * @param close 收盘价格
     * @param previousRSI   前一个rsi
     * @param interval  间隔
     * @return  rsi
     */
    public static double calculateRSI(double open, double close, double previousRSI, int interval) {
        double gain = Math.max(0, close - open);
        double loss = Math.max(0 , open - close);
        double avgGain = (gain + (interval - 2) * previousRSI) / (interval - 1);
        double avgLoss = (loss + (interval - 2) * (100 - previousRSI)) / (interval - 1);

        return 100 - (100/(1 + avgGain/avgLoss));
    }



    /**
     * 计算趋势线，采用最小二乘法
     * @param data data
     * @param getY getY
     * @param getX getX
     * @return TrendLine
     */
    public static<T> TrendLine calculateTrend(List<T> data, Function<T, Double> getX, Function<T, Double> getY) {
        if (data == null || data.isEmpty() || data.size() < 2) {
            return new TrendLine(0,0);
        }
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (T kLine : data) {
            double y =  getY.apply(kLine);
//            long x = kLine.getOpenTime().toInstant(ZoneOffset.UTC).getEpochSecond();
            double x = getX.apply(kLine);

            sumY += y;
            sumX += x;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double k = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double m = (sumY - k * sumX) / n;

        return  new TrendLine(k, m);
    }


    /**
     * 计算list的单调性，从后往前取出具有单调性的部分
     * @param arr arr
     * @param calField 要计算单调性的字段
     * @param <T> 范型
     * @return 从后往前具有单调性的数组
     */
    public static <T> List<T> getLastMonotonicPart(List<T> arr, Function<T, Double> calField) {
        if (arr == null || arr.size() < 2) {
            return null;
        }

        int n = arr.size();
        int count = 1;

        // 从倒数第二个元素开始，比较相邻元素的大小
        boolean isIncreasing = calField.apply(arr.get(n - 2)) <= calField.apply(arr.get(n - 1));  // 判断单调性（递增或递减）

        // 从倒数第二个元素往前遍历
        for (int i = n - 2; i >= 0; i--) {
            if (isIncreasing) {
                // 如果是递增序列
                if (calField.apply(arr.get(i)) > calField.apply(arr.get(i + 1))) {
                    break;
                }

            } else {
                // 如果是递减序列
                if (calField.apply(arr.get(i)) > calField.apply(arr.get(i + 1))) {
                    break;
                }
            }
            count++;
        }

        // 返回最后具有单调性的部分
        return arr.subList(n - Math.max(2, count), n);
    }
}
