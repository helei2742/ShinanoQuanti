

package com.helei.tradedatacenter.indicator.calculater;

        import com.helei.tradedatacenter.entity.KLine;
        import com.helei.tradedatacenter.indicator.RSI;
        import com.helei.tradedatacenter.util.CalculatorUtil;
        import org.apache.flink.api.common.state.ValueState;
        import org.apache.flink.api.common.state.ValueStateDescriptor;
        import org.apache.flink.configuration.Configuration;

        import java.io.IOException;

public class RSICalculator extends BaseIndicatorCalculator<RSI>{

    private final int period;

    private ValueState<Double> rsiState;

    public RSICalculator(int period) {
        this.period = period;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        rsiState = getRuntimeContext().getState(new ValueStateDescriptor<>("rsiState", Double.class));
    }

    @Override
    public String indicatorKey(RSI indicator) {
        return "RSI-" + period;
    }

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
