

package com.helei.tradedatacenter.indicator;


        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.NoArgsConstructor;

/**
 * MA指标数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MA implements Indicator {


    /**
     * 值
     */
    private Double ma;

    @Override
    public MA clone() {
        return new MA(ma);
    }
}
