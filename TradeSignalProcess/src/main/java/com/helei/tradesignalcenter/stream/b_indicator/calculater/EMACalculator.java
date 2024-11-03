package com.helei.tradesignalcenter.stream.b_indicator.calculater;

import com.helei.dto.trade.KLine;
import com.helei.dto.indicator.EMA;
import com.helei.dto.indicator.config.EMAConfig;
import com.helei.util.CalculatorUtil;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;


public class EMACalculator extends BaseIndicatorCalculator<EMA> {
    private final int period;

    private ValueState<Double> emaState;

    public EMACalculator(EMAConfig emaConfig) {
        super(emaConfig);
        this.period = emaConfig.getPeriod();
    }

    @Override
    public void open(Configuration parameters, RuntimeContext runtimeContext) throws Exception {
        emaState = runtimeContext.getState(new ValueStateDescriptor<>("emaState", Double.class));
    }


    @Override
    public EMA calculateInKLine(KLine kLine) throws IOException {
        Double ema = emaState.value();
        Double close = kLine.getClose();
        if (ema == null) {
            ema = close;
        }

        ema = CalculatorUtil.calculateEMA(close, ema, period);
        emaState.update(ema);
        return new EMA(ema);
    }
}

