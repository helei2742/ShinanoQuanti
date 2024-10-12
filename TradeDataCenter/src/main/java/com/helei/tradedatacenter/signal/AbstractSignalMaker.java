package com.helei.tradedatacenter.signal;

import cn.hutool.core.util.BooleanUtil;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;


/**
 * 信号生成器的抽象类，会把传入的KLine分为两类
 * 1. 已完结的k线数据， kLine.end = true
 *      这样的k线数据，可以认为是历史k线数据，可用于状态更新。
 * 2、实时的k线数据， kLine.end = false
 *      实时数据，用于决策是否产出信号
 */
@Slf4j
public abstract class AbstractSignalMaker extends KeyedProcessFunction<String, KLine, TradeSignal> {
    @Override
    public void processElement(KLine kLine, KeyedProcessFunction<String, KLine, TradeSignal>.Context context, Collector<TradeSignal> collector) throws Exception {
        try {
            if (BooleanUtil.isTrue(kLine.isEnd())) {
                stateUpdate(kLine);
            } else {
                TradeSignal signal = buildSignal(kLine);
                if (signal != null) {
                    collector.collect(signal);
                }
            }
        } catch (Exception e) {
            log.error("build signal error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新状态，传入的k线是已完结的k线数据
     * @param kLine 已完结的k线数据
     */
    protected abstract void stateUpdate(KLine kLine) throws IOException;

    /**
     * 产生信号
     * @param kLine 实时推送的k线数据
     * @return 交易信号
     */
    protected abstract TradeSignal buildSignal(KLine kLine) throws IOException;
}
