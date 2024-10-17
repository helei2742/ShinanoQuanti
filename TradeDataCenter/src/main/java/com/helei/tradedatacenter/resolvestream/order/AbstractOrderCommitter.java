package com.helei.tradedatacenter.resolvestream.order;

import com.helei.tradedatacenter.dto.OriginOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

@Slf4j
public abstract class AbstractOrderCommitter extends RichSinkFunction<OriginOrder> {

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void invoke(OriginOrder order, Context context) throws Exception {

        if (commitTradeOrder(order)) {
            log.info("Order committed successfully");
        } else {
            log.error("Order committed error");
            //TODO 重试？？
        }
    }

    public abstract boolean commitTradeOrder(OriginOrder order);
}
