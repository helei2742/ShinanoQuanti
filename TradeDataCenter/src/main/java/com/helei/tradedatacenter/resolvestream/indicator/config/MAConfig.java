package com.helei.tradedatacenter.resolvestream.indicator.config;

        import com.helei.tradedatacenter.resolvestream.indicator.MA;
        import lombok.Getter;

@Getter
public class MAConfig extends IndicatorConfig<MA>{
    private final int period;

    public MAConfig(final int period) {
        super(MA.class);
        this.period = period;
    }

    @Override
    public String getIndicatorName() {
        return name + "-" + period;
    }
}
