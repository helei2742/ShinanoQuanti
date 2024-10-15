

package com.helei.tradedatacenter.resolvestream.indicator;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EMA
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EMA implements Indicator {

    /**
     * ema值
     */
    private Double ema;


    @Override
    public EMA clone() {
        return new EMA(ema);
    }
}
