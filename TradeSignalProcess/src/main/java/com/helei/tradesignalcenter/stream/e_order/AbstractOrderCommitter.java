package com.helei.tradesignalcenter.stream.e_order;

import com.helei.tradesignalcenter.config.TradeSignalConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink2.Sink;

/**
 * 订单提交服务
 */
@Slf4j
public abstract class AbstractOrderCommitter<T> {

    protected TradeSignalConfig tradeSignalConfig;


    public AbstractOrderCommitter() {
        tradeSignalConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG;
    }


    public abstract Sink<T> getCommitSink();
}
