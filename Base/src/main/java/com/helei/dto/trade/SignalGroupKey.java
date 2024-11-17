package com.helei.dto.trade;

import com.helei.constants.trade.KLineInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SignalGroupKey {
    private String symbol;

    private KLineInterval interval;

    private long openTime;

    public String getStreamKey() {
        return KLine.getKLineStreamKey(symbol, interval);
    }
}
