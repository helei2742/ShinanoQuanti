package com.helei.dto.kafka;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 交易信号的topic
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeSignalTopic {

    private RunEnv runEnv;

    private TradeType tradeType;

    private String symbol;

    private String name;

    public String toString() {
        return (runEnv.name() + "." + tradeType + "." + symbol + "." + name).toLowerCase();
    }
}
