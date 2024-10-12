
package com.helei.tradedatacenter.datasource;

import com.helei.tradedatacenter.constants.KLineInterval;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.dto.SubscribeData;
import com.helei.tradedatacenter.entity.KLine;
import lombok.extern.slf4j.Slf4j;



/**
 * 内存的k线数据源
 */
@Slf4j
public class MemoryKLineSource extends BaseKLineSource {
    private final MemoryKLineDataPublisher memoryKLineDataPublisher;

    private final SubscribeData subscribeData;

    public MemoryKLineSource(
            String symbol,
            KLineInterval interval,
            MemoryKLineDataPublisher memoryKLineDataPublisher
    ) {
        this.memoryKLineDataPublisher = memoryKLineDataPublisher;
        this.subscribeData = memoryKLineDataPublisher.registry(symbol, interval);
    }


    @Override
    protected KLine loadKLine() throws Exception {
        return KLineMapper.mapJsonToKLine(subscribeData.getData());
    }

}