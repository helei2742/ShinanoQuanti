package com.helei.tradedatacenter.datasource;

import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.entity.KLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * 内存的k线数据源
 */
@Slf4j
public class MemoryKLineSource implements SourceFunction<KLine> {
    private static final LinkedBlockingQueue<JSONObject> blockingQueue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning = true;

    public void append(JSONObject kline) throws InterruptedException {
        blockingQueue.offer(kline);
    }

    @Override
    public void run(SourceContext<KLine> ctx) throws Exception {
        JSONObject originKLine = null;
        while (isRunning) {
            originKLine = blockingQueue.poll(100, TimeUnit.MILLISECONDS);

            if (originKLine != null) {
                KLine kLine = KLineMapper.mapJsonToKLine(originKLine);

//                if (kLine.isEnd()) {
                ctx.collect(kLine);
//                    log.debug("append end kLine [{}]", kLine);
//                }
            } else {
//                log.warn("origin kLine data warn, originKLine={}", originKLine);
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
