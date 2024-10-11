


package com.helei.tradedatacenter.util;

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
}
