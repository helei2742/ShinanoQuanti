package com.helei.tradesignalprocess.stream.e_trade_signal;

import com.helei.tradesignalprocess.config.TradeSignalConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink2.Sink;

/**
 * 订单提交服务
 */
@Slf4j
public abstract class AbstractTradeSignalCommitter<T> {

    protected TradeSignalConfig tradeSignalConfig;


    public AbstractTradeSignalCommitter() {
        tradeSignalConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG;
    }


    public abstract Sink<T> getCommitSink();
}
