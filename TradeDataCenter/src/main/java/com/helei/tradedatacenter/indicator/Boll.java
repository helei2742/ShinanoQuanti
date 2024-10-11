

package com.helei.tradedatacenter.indicator;


        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Boll implements Indicator{

    private Double sma;

    private Double upper;

    private Double lower;

    @Override
    public Indicator clone() {
        return null;
    }
}
