package com.helei.tradesignalprocess.stream.b_indicator.calculater;

import com.helei.dto.trade.KLine;
import com.helei.dto.indicator.RSI;
import com.helei.dto.indicator.config.RSIConfig;
import com.helei.util.CalculatorUtil;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;

public class RSICalculator extends BaseIndicatorCalculator<RSI> {
    private final int period;

    private ValueState<Double> rsiState;

    public RSICalculator(RSIConfig rsiConfig) {
        super(rsiConfig);
        this.period = rsiConfig.getPeriod();
    }

    @Override
    public void open(Configuration parameters, RuntimeContext runtimeContext) throws Exception {
        rsiState = runtimeContext.getState(new ValueStateDescriptor<>(indicatorConfig.getIndicatorName() + "_rsiState", Double.class));
    }

//    @Override
//    public String indicatorKey(RSI indicator) {
//        return "RSI-" + period;
//    }

    @Override
    public RSI calculateInKLine(KLine kLine) throws IOException {
        Double rsi = rsiState.value();

        if (rsi == null) {
            rsi = 50.0;
        }

        Double open = kLine.getOpen();
        Double close = kLine.getClose();


        rsi = CalculatorUtil.calculateRSI(open, close, rsi, period);

        rsiState.update(rsi);

        return new RSI(rsi);
    }
}
