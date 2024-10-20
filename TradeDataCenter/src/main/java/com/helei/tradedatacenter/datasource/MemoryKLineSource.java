package com.helei.tradedatacenter.datasource;

import com.helei.constants.KLineInterval;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.util.KLineBuffer;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * 内存的k线数据源
 */
@Slf4j
public class MemoryKLineSource extends BaseKLineSource {
    private static final Map<String, KLineBuffer> kLineBufferMap = new HashMap<>();


    private final String id = UUID.randomUUID().toString();


    private final String symbol;


    public MemoryKLineSource(
            String symbol,
            KLineInterval interval,
            LocalDateTime startTime,
            MemoryKLineDataPublisher memoryKLineDataPublisher) {
        super(interval);
        this.symbol = symbol;
        kLineBufferMap.put(id, memoryKLineDataPublisher.registry(symbol, interval, startTime));
    }


    @Override
    protected KLine loadKLine() throws Exception {
        KLineBuffer kLineBuffer = kLineBufferMap.get(id);
        if (kLineBuffer == null) {
            log.error("didn't registry kline[{}]-[{}] on memoryKLineDataPublisher", symbol, kLineInterval.getDescribe());
        }
        return kLineBuffer.take();
    }
}
