package com.helei.dto;

import com.helei.constants.KLineInterval;
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

    public String getStreamKey() {
        return KLine.getKLineStreamKey(symbol, interval);
    }
}
