package com.helei.dto.indicator;

import lombok.*;

        import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MACD implements Indicator {
    @Serial
    private static final long serialVersionUID = -5187464594186468L; // 显式声明 serialVersionUID

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
