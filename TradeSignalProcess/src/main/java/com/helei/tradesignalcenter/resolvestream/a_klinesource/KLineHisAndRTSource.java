package com.helei.tradesignalcenter.resolvestream.a_klinesource;

import cn.hutool.core.util.StrUtil;
import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.config.TradeSignalConfig;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.List;
import java.util.Set;

public abstract class KLineHisAndRTSource extends RichSourceFunction<KLine> {

    protected final TradeSignalConfig tradeSignalConfig;

    protected final String symbol;

    protected final Set<KLineInterval> intervals;

    protected final long startTime;

    protected volatile boolean isRunning = true;

    protected KLineHisAndRTSource(String symbol, Set<KLineInterval> intervals, long startTime) {
        this.symbol = symbol;
        this.intervals = intervals;
        this.startTime = startTime;
        this.tradeSignalConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG;

        if (!argsCheck(symbol, intervals, startTime)) {
            throw new IllegalArgumentException("KLineHisAndRTSource传入参数错误");
        }
    }

    private boolean argsCheck(String symbol, Set<KLineInterval> intervals, long startTime) {
        return StrUtil.isNotBlank(symbol) && intervals != null && !intervals.isEmpty() && startTime > 0;
    }
}
