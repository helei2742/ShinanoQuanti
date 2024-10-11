package com.helei.tradedatacenter.indicator.calculater;

import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.Boll;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;

import java.util.LinkedList;

public class BollCalculator extends BaseIndicatorCalculator<Boll> {

    private final int period;

    private transient ListState<Double> priceListState;

    public BollCalculator(int period) {
        this.period = period;
    }

    @Override
    public void open(Configuration parameters) throws Exception {

        priceListState = getRuntimeContext().getListState(new ListStateDescriptor<>("priceListState", Double.class));
    }

    @Override
    public String indicatorKey(Boll indicator) throws Exception {

        return "BOLL-" + period;
    }

    @Override
    public Boll calculateInKLine(KLine kLine) throws Exception {
        LinkedList<Double> priceList = new LinkedList<>();
        for (Double price : priceListState.get()) {
            priceList.add(price);
        }
        priceList.add(kLine.getClose());

        while (priceList.size() > period) {
            priceList.remove(0);
        }
        priceListState.update(priceList);


        // 计算 SMA
        if (priceList.size() == period) {
            double sma = calculateSMA(priceList);

            // 计算标准差
            double stddev = calculateStandardDeviation(priceList, sma);

            // 计算布林带上下轨
            double upperBand = sma + 2 * stddev;
            double lowerBand = sma - 2 * stddev;

            return new Boll(sma, upperBand, lowerBand);
        }
        return null;
    }

    // 计算简单移动平均线（SMA）
    private double calculateSMA(LinkedList<Double> prices) {
        double sum = 0.0;
        for (Double price : prices) {
            sum += price;
        }
        return sum / prices.size();
    }

    // 计算标准差
    private double calculateStandardDeviation(LinkedList<Double> prices, double sma) {
        double sumSquaredDiffs = 0.0;
        for (Double price : prices) {
            sumSquaredDiffs += Math.pow(price - sma, 2);
        }
        return Math.sqrt(sumSquaredDiffs / prices.size());
    }

}
