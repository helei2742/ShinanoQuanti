package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.concurrent.TimeUnit;

public abstract class BaseKLineSource extends RichSourceFunction<KLine> {
    private volatile boolean isRunning = true;

    public final KLineInterval kLineInterval;

    protected BaseKLineSource(KLineInterval kLineInterval) {
        this.kLineInterval = kLineInterval;
    }

    @Override
    public void run(SourceContext<KLine> sourceContext) throws Exception {
        isRunning = init(sourceContext);
        while (isRunning) {
            TimeUnit.MINUTES.sleep(50);

            refreshState();
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    abstract boolean init(SourceContext<KLine> sourceContext) throws Exception;

    abstract void refreshState();

}


