package com.helei.tradedatacenter.resolvestream.indicator.config;

import com.helei.tradedatacenter.resolvestream.indicator.PST;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class PSTConfig extends IndicatorConfig<PST> {
    private final int pressureCount;

    private final int supportCount;

    private final int windowLength;


    public PSTConfig(int windowLength, int pressureCount, int supportCount) {
        super(PST.class);
        this.pressureCount = pressureCount;
        this.supportCount = supportCount;
        this.windowLength = windowLength;
    }

    @Override
    public String getIndicatorName() {
        return name + "-" + windowLength + "-" + supportCount + "-" + pressureCount;
    }
}


