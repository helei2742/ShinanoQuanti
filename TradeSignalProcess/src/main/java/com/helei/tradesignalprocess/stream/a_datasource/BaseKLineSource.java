package com.helei.tradesignalprocess.stream.a_datasource;

import com.helei.constants.KLineInterval;
import com.helei.dto.trade.KLine;
import lombok.Getter;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Deprecated

public abstract class BaseKLineSource extends RichSourceFunction<KLine> {
    private static final String DISPATCHER = ",";

    private volatile boolean isRunning = true;

    private final String intervalJoinStr;

    @Getter
    private final String symbol;

    protected BaseKLineSource(List<KLineInterval> kLineInterval, String symbol) {
        this.intervalJoinStr = buildIntervalJoinStr(kLineInterval);
        this.symbol = symbol.toUpperCase();
    }

    @Override
    public void run(SourceContext<KLine> sourceContext) throws Exception {
        isRunning = loadData(sourceContext);
        while (isRunning) {
            TimeUnit.MINUTES.sleep(50);

            refreshState();
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    abstract boolean loadData(SourceContext<KLine> sourceContext) throws Exception;

    abstract void refreshState();


    protected String buildIntervalJoinStr(List<KLineInterval> intervals) {
        return intervals.stream().map(KLineInterval::getDescribe).collect(Collectors.joining(DISPATCHER));
    }

    /**
     * 获取KLineSource的k线频率
     * @return List < KLineInterval>=
     */
    public List<KLineInterval> getIntervals() {
        return Arrays.stream(intervalJoinStr.split(DISPATCHER)).map(KLineInterval.STATUS_MAP::get).collect(Collectors.toList());
    }
}


