package com.helei.dto.indicator;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RSI implements Indicator {
    @Serial
    private static final long serialVersionUID = -54898746849844784L; // 显式声明 serialVersionUID


    private Double rsi;

    @Override
    public Indicator clone() {
        return new RSI(rsi);
    }
}
