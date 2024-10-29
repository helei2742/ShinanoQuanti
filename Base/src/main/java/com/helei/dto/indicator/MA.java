package com.helei.dto.indicator;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * MA指标数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MA implements Indicator {

    @Serial
    private static final long serialVersionUID = -1516541354154L; // 显式声明 serialVersionUID

    /**
     * 值
     */
    private Double ma;

    @Override
    public MA clone() {
        return new MA(ma);
    }
}
