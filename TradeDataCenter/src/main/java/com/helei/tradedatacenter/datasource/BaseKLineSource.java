package com.helei.tradedatacenter.datasource;

import com.helei.tradedatacenter.entity.KLine;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public abstract class BaseKLineSource implements SourceFunction<KLine> {
    private volatile boolean isRunning = true;

    @Override
    public void run(SourceContext<KLine> sourceContext) throws Exception {
        while (isRunning) {
            KLine kLine = loadKLine();

            if (kLine != null) {
                sourceContext.collect(kLine);
            }
        }
    }

    protected abstract KLine loadKLine() throws Exception;


    @Override
    public void cancel() {
        isRunning = false;
    }
}