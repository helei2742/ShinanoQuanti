package com.helei.tradedatacenter.resolvestream.indicator.config;

        import com.helei.tradedatacenter.resolvestream.indicator.EMA;
        import lombok.EqualsAndHashCode;
        import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class EMAConfig extends IndicatorConfig<EMA>{
    private final int period;

    public EMAConfig(int period) {
        super(EMA.class);
        this.period = period;
    }

    @Override
    public String getIndicatorName() {
        return name + "-" +period;
    }
}
