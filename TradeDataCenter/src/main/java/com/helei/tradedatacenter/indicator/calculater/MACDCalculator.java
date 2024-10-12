
package com.helei.tradedatacenter.indicator.calculater;

import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.MACD;
import com.helei.tradedatacenter.util.CalculatorUtil;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class MACDCalculator extends BaseIndicatorCalculator<MACD> {

    private int ema1Period = 12;

    private int ema2Period = 26;

    private int deaPeriod = 9;

    private transient ValueState<MACD> macdState;


    public MACDCalculator(String name, int ema1Period, int ema2Period, int deaPeriod) {
        super(name);
        this.ema1Period = ema1Period;
        this.ema2Period = ema2Period;
        this.deaPeriod = deaPeriod;
    }


    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        ValueStateDescriptor<MACD> descriptor = new ValueStateDescriptor<>("macdState", TypeInformation.of(MACD.class));
        macdState = getRuntimeContext().getState(descriptor);
    }

//    @Override
//    public String indicatorKey(MACD indicator) {
//        return "MACE" +  ema1Period + "-"  + ema2Period + "-" + deaPeriod;
//    }

    @Override
    public MACD calculateInKLine(KLine kLine) throws IOException {
        MACD macd = macdState.value();
        double ema1 = macd.getEma1();
        double ema2 = macd.getEma2();
        double dea = macd.getDea();

        // 计算新的 EMA12 和 EMA26
        ema1 = CalculatorUtil.calculateEMA(kLine.getClose(), ema1, ema1Period);
        ema2 = CalculatorUtil.calculateEMA(kLine.getClose(), ema2, ema2Period);

        // 计算 DIF
        double dif = ema1 - ema2;
        dea = CalculatorUtil.calculateEMA(dif, dea, deaPeriod);

        macd.setEma1(ema1);
        macd.setEma2(ema2);
        macd.setDea(dea);
        macdState.update(macd);
        return macd;
    }

}