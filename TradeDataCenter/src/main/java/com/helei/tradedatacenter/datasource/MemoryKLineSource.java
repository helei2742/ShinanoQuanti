
package com.helei.tradedatacenter.datasource;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.constants.KLineInterval;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.dto.SubscribeData;
import com.helei.tradedatacenter.entity.KLine;
import lombok.extern.slf4j.Slf4j;



/**
 * 内存的k线数据源
 */
@Slf4j
public class MemoryKLineSource extends BaseKLineSource {

    private final SubscribeData subscribeData;

    public MemoryKLineSource(
            String symbol,
            KLineInterval interval,
            MemoryKLineDataPublisher memoryKLineDataPublisher
    ) {
        this.subscribeData = memoryKLineDataPublisher.registry(symbol, interval);
    }


    @Override
    protected KLine loadKLine() throws Exception {
        JSONObject data = subscribeData.getData();

        return data == null ? null: KLineMapper.mapJsonToKLine(data);
    }
}
