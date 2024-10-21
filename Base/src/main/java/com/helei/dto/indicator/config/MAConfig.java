package com.helei.dto.indicator.config;

        import com.helei.dto.indicator.MA;
        import lombok.EqualsAndHashCode;
        import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
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
