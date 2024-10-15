package com.helei.tradedatacenter.resolvestream.indicator.config;

        import com.helei.tradedatacenter.resolvestream.indicator.MACD;
        import lombok.Getter;

@Getter
public class MACDConfig extends IndicatorConfig<MACD>{
    private int ema1Period = 12;

    private int ema2Period = 26;

    private int deaPeriod = 9;

    public MACDConfig(int ema1Period, int ema2Period, int deaPeriod) {
        super(MACD.class);
        this.ema1Period = ema1Period;
        this.ema2Period = ema2Period;
        this.deaPeriod = deaPeriod;
    }

    public MACDConfig() {
        super(MACD.class);
    }

    @Override
    public String getIndicatorName() {
        return name + "-" + ema1Period + "-" + ema2Period + "-" + deaPeriod;
    }
}
