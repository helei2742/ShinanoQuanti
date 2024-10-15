package com.helei.tradedatacenter.resolvestream.decision;

import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.tradedatacenter.entity.TradeSignal;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public abstract class AbstractDecisionMaker extends ProcessFunction<TradeSignal, BaseOrder> {

    @Override
    public void processElement(TradeSignal tradeSignal, ProcessFunction<TradeSignal, BaseOrder>.Context context, Collector<BaseOrder> collector) throws Exception {

    }

    public abstract BaseOrder decisionAndBuilderOrder(TradeSignal signal);
}