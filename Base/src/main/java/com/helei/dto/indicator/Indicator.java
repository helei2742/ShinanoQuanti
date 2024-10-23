

package com.helei.dto.indicator;

        import java.io.Serial;
        import java.io.Serializable;

/**
 * 指标接口
 */
public interface Indicator extends Serializable {

    @Serial
    public static final long serialVersionUID = 8767865645L; // 显式声明 serialVersionUID

    Indicator clone();
}
