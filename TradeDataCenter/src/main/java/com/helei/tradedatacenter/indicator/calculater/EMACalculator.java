

package com.helei.tradedatacenter.indicator.calculater;

import com.helei.tradedatacenter.entity.KLine;
        import com.helei.tradedatacenter.indicator.EMA;
        import com.helei.tradedatacenter.util.CalculatorUtil;
        import org.apache.flink.api.common.state.ValueState;
        import org.apache.flink.api.common.state.ValueStateDescriptor;
        import org.apache.flink.configuration.Configuration;

        import java.io.IOException;

public class EMACalculator extends BaseIndicatorCalculator<EMA>{
    private final int period;

    private ValueState<Double> emaState;

    public EMACalculator(int period) {
        this.period = period;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        emaState = getRuntimeContext().getState(new ValueStateDescriptor<>("emaState", Double.class));
    }

    @Override
    public String indicatorKey(EMA indicator) {
        return "EMA-"+period;
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
