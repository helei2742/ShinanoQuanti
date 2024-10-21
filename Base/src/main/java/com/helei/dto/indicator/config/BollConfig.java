
package com.helei.dto.indicator.config;


import com.helei.dto.indicator.Boll;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class BollConfig extends IndicatorConfig<Boll> {

    private final int period;

    public BollConfig(int period) {
        super(Boll.class);
        this.period = period;
    }

    @Override
    public String getIndicatorName() {
        return name + "-" + period;
    }
}

