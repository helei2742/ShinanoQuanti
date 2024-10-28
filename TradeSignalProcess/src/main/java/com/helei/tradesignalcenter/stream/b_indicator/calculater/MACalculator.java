package com.helei.tradesignalcenter.stream.b_indicator.calculater;

import com.helei.dto.KLine;
import com.helei.dto.indicator.MA;
import com.helei.dto.indicator.config.MAConfig;
import com.helei.util.CalculatorUtil;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;

/**
 * SMA
 */
public class MACalculator extends BaseIndicatorCalculator<MA> {

    private final int period;

    private transient ValueState<Double> maState;

    public MACalculator(MAConfig maConfig) {
        super(maConfig);
        this.period = maConfig.getPeriod();
    }

    @Override
    public void open(Configuration parameters, RuntimeContext runtimeContext) throws Exception {
        this.maState = runtimeContext.getState(new ValueStateDescriptor<>("maState", Double.class));
    }


    @Override
    public MA calculateInKLine(KLine kLine) throws IOException {
        Double ma = this.maState.value();
        Double close = kLine.getClose();

        if (ma == null) {
            ma = close;
        }

        ma = CalculatorUtil.calculateMA(close, ma, period);

        maState.update(ma);
        return new MA(ma);
    }
}
