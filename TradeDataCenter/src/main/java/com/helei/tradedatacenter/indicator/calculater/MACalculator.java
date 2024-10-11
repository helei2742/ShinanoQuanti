
package com.helei.tradedatacenter.indicator.calculater;

        import com.helei.tradedatacenter.entity.KLine;
        import com.helei.tradedatacenter.indicator.MA;
        import com.helei.tradedatacenter.util.CalculatorUtil;
        import org.apache.flink.api.common.state.ValueState;
        import org.apache.flink.api.common.state.ValueStateDescriptor;
        import org.apache.flink.configuration.Configuration;

        import java.io.IOException;

/**
 * SMA
 */
public class MACalculator extends BaseIndicatorCalculator<MA>{

    private final int period;

    private transient ValueState<Double> maState;

    public MACalculator(int period) {
        this.period = period;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.maState = getRuntimeContext().getState(new ValueStateDescriptor<>("maState", Double.class));
    }


    @Override
    public String indicatorKey(MA indicator) {
        return "MA-" + period;
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

