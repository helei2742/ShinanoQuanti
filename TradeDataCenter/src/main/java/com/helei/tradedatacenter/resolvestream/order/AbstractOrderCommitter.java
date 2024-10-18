package com.helei.tradedatacenter.resolvestream.order;

import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.tradedatacenter.dto.OriginOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

@Slf4j
public abstract class AbstractOrderCommitter extends RichAsyncFunction<OriginOrder, BaseOrder> {

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void asyncInvoke(OriginOrder originOrder, ResultFuture<BaseOrder> resultFuture) throws Exception {

    }

    public abstract boolean commitTradeOrder(OriginOrder order);
}
