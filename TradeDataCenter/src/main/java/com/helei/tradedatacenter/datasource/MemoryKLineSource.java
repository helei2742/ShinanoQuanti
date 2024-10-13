
package com.helei.tradedatacenter.datasource;

import com.helei.cexapi.binanceapi.constants.KLineInterval;
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
    private final String id = UUID.randomUUID().toString();

    private static final Map<String, KLineBuffer> kLineBufferMap = new HashMap<>();

    public MemoryKLineSource(
            String symbol,
            KLineInterval interval,
            LocalDateTime startTime,
            MemoryKLineDataPublisher memoryKLineDataPublisher
    ) {
        kLineBufferMap.put(id, memoryKLineDataPublisher.registry(symbol, interval, startTime));
    }


    @Override
    protected KLine loadKLine() throws Exception {
        KLineBuffer kLineBuffer = kLineBufferMap.get(id);
        return kLineBuffer.take();
    }
}
