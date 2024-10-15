package com.helei.tradedatacenter.resolvestream.indicator.config;

        import com.helei.tradedatacenter.resolvestream.indicator.RSI;
        import lombok.Getter;

@Getter
public class RSIConfig extends IndicatorConfig<RSI> {
    private final int period;

    public RSIConfig(int period) {
        super(RSI.class);
        this.period = period;
    }

    @Override
    public String getIndicatorName() {
        return name + "-" + period;
    }
}


