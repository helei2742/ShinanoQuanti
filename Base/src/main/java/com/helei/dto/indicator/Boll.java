package com.helei.dto.indicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Boll implements Indicator {

    @Serial
    private static final long serialVersionUID = -21358767865645L; // 显式声明 serialVersionUID

    private Double sma;

    private Double upper;

    private Double lower;

    @Override
    public Indicator clone() {
        return null;
    }
}
