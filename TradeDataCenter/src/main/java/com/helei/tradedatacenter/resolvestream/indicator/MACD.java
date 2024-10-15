package com.helei.tradedatacenter.resolvestream.indicator;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MACD implements Indicator {

    private Double ema1 = 0.0;

    private Double ema2 = 0.0;

    /**
     * macd dea 慢线
     */
    private Double dea = 0.0;

    /**
     * macd dif 快线
     * @return dif值
     */
    public Double dif() {
        return ema1 - ema2;
    }

    /**
     * macd柱状图
     * @return 高度
     */
    public Double macdHistogram() {
        return  2 * (dif() - dea);
    }

    @Override
    public Indicator clone() {
        return new MACD(ema1, ema2, dea);
    }
}
