package com.helei.tradesignalcenter.stream.a_datasource;

import com.helei.tradesignalcenter.dto.UserAccountEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Deprecated
@Slf4j
public abstract class AccountEventSource extends RichSourceFunction<UserAccountEvent> {

    private volatile boolean isRunning = true;


    @Override
    public void run(SourceContext<UserAccountEvent> sourceContext) throws Exception {
        isRunning = init(sourceContext);
        while (isRunning) {
            TimeUnit.MINUTES.sleep(50);

            refreshState();
        }
        log.warn("账户事件流关闭");
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    abstract boolean init(SourceContext<UserAccountEvent> sourceContext) throws ExecutionException, InterruptedException;

    abstract void refreshState();
}
