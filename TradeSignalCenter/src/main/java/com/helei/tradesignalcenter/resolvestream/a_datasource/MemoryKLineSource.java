package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.util.KLineBuffer;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * 内存的k线数据源
 */
@Slf4j
public class MemoryKLineSource extends BaseKLineSource {

    private transient MemoryKLineDataPublisher dataPublisher;

    private transient KLineBuffer kLineBuffer;

    private transient VirtualThreadTaskExecutor publishExecutor;

    private final long startTime;

    private final String streamUrl;

    private final String requestUrl;

    private final int bufferSize;

    private final int historyLoadBatch;

    private final String symbol;

    @Override
    public void open(OpenContext openContext) throws Exception {
        publishExecutor = new VirtualThreadTaskExecutor("kline-load-executor");

        log.info("开始初始化KLineDataPublisher");
        dataPublisher = new MemoryKLineDataPublisher(
                streamUrl,
                requestUrl,
                bufferSize,
                historyLoadBatch,
                publishExecutor
                );

        log.info("开始注册监听的k线");
        dataPublisher.addListenKLine(symbol, List.of(kLineInterval));
        log.info("监听k线注册成功");
        kLineBuffer =  dataPublisher.registry(symbol, kLineInterval, startTime);
    }

    public MemoryKLineSource(
            String symbol,
            KLineInterval interval,
            long startTime,
            String streamUrl,
            String requestUrl,
            int bufferSize,
            int historyLoadBatch
    ) {
        super(interval);
        this.symbol = symbol;
        this.startTime = startTime;
        this.streamUrl = streamUrl;
        this.requestUrl = requestUrl;
        this.bufferSize = bufferSize;
        this.historyLoadBatch = historyLoadBatch;
    }

    @Override
    boolean init(SourceContext<KLine> sourceContext) throws ExecutionException, InterruptedException {
        publishExecutor.execute(()->{
            while (true) {
                try {
                    KLine take = kLineBuffer.take();

                    sourceContext.collect(take);
                } catch (InterruptedException e) {
                    log.error("取kLine数据发生错误", e);
                    System.exit(-1);
                }
            }
        });
        return false;
    }

    @Override
    void refreshState() {

    }
}
