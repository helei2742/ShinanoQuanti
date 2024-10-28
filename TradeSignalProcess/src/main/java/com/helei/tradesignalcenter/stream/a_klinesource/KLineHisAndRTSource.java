package com.helei.tradesignalcenter.stream.a_klinesource;

import cn.hutool.core.util.StrUtil;
import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.config.TradeSignalConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class KLineHisAndRTSource extends RichSourceFunction<KLine> {

    /**
     * k线数据缓冲区
     */
    private transient BlockingQueue<KLine> buffer;

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

    @Override
    public final void open(Configuration parameters) throws Exception {
        buffer = new LinkedBlockingQueue<>();
        onOpen(parameters);
    }

    @Override
    public final void run(SourceContext<KLine> sourceContext) throws Exception {
        loadDataInBuffer(buffer);
        //Step 4: 阻塞获取，等待结束
        while (isRunning) {
            KLine kLine = buffer.poll(50, TimeUnit.MINUTES);
            if (kLine != null) {
                synchronized (sourceContext.getCheckpointLock()) {
                    sourceContext.collect(kLine);
                }
            }
        }
    }
    @Override
    public final void cancel() {
        isRunning = false;
        onCancel();
    }


    protected abstract void loadDataInBuffer(BlockingQueue<KLine> buffer);

    protected abstract void onOpen(Configuration parameters) throws Exception;

    protected void onCancel() {

    }

    private boolean argsCheck(String symbol, Set<KLineInterval> intervals, long startTime) {
        return StrUtil.isNotBlank(symbol) && intervals != null && !intervals.isEmpty() && startTime > 0;
    }
}
