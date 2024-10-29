package com.helei.dto.indicator;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * EMA
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EMA implements Indicator {


    @Serial
    private static final long serialVersionUID = -128328476328234L; // 显式声明 serialVersionUID

    /**
     * ema值
     */
    private Double ema;


    @Override
    public EMA clone() {
        return new EMA(ema);
    }
}
