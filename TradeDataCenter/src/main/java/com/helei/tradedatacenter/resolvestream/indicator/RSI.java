
package com.helei.tradedatacenter.resolvestream.indicator;


        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RSI implements Indicator{

    private Double rsi;

    @Override
    public Indicator clone() {
        return new RSI(rsi);
    }
}
