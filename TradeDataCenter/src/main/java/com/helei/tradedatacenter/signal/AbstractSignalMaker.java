package com.helei.tradedatacenter.signal;

import cn.hutool.core.util.BooleanUtil;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;


/**
 * 信号生成器的抽象类，会把传入的KLine分为两类
 * 1. 已完结的k线数据， kLine.end = true
 * 这样的k线数据，可以认为是历史k线数据，可用于状态更新。
 * 2、实时的k线数据， kLine.end = false
 * 实时数据，用于决策是否产出信号
 */
@Slf4j
public abstract class AbstractSignalMaker extends KeyedProcessFunction<String, KLine, TradeSignal> {

    /**
     * 是否是一条k线只发出一个信号
     */
    private final boolean isAKLineSendOneSignal;

    /**
     * 当前k线，就是buildSignal(kline) 参数kline同意openTime的k线
     */
    private ValueState<KLine> curKLine;

    /**
     * 当前是否发出过信号
     */
    private ValueState<Boolean> isCurSendSignal;

    protected AbstractSignalMaker(boolean isAKLineSendOneSignal) {
        this.isAKLineSendOneSignal = isAKLineSendOneSignal;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        curKLine = getRuntimeContext().getState(new ValueStateDescriptor<>("currentKLine", TypeInformation.of(KLine.class)));
        isCurSendSignal = getRuntimeContext().getState(new ValueStateDescriptor<>("isCurSendSignal", Boolean.class));

        this.onOpen(openContext);
    }

    @Override
    public void processElement(KLine kLine, KeyedProcessFunction<String, KLine, TradeSignal>.Context context, Collector<TradeSignal> collector) throws Exception {
        try {
            if (BooleanUtil.isTrue(kLine.isEnd())) {
                stateUpdate(kLine);
            } else {
                TradeSignal signal = buildSignal(kLine);
                if (signal == null) return;

                if (isAKLineSendOneSignal && BooleanUtil.isTrue(isCurSendSignal.value())) {
                    //当前k线发送过信号
                    log.info("已发送过macd信号");
                } else {
                    isCurSendSignal.update(true);
                    collector.collect(signal);
                    log.info("signal maker send a signal: [{}]", signal);
                }
            }
        } catch (Exception e) {
            log.error("build signal error", e);
            throw new RuntimeException(e);
        }

        updateCurKLine(kLine);
    }

    /**
     * onOpen.定义state的初始化等
     *
     * @param openContext openContext
     * @throws Exception Exception
     */
    public abstract void onOpen(OpenContext openContext) throws Exception;


    /**
     * 更新状态，传入的k线是已完结的k线数据
     *
     * @param kLine 已完结的k线数据
     */
    protected abstract void stateUpdate(KLine kLine) throws IOException;

    /**
     * 产生信号
     *
     * @param kLine 实时推送的k线数据
     * @return 交易信号
     */
    protected abstract TradeSignal buildSignal(KLine kLine) throws IOException;


    /**
     * 更新当前k线，如果成功更新，还要将isSendSignal设置为false
     *
     * @param cur cur
     * @throws IOException IOException
     */
    private void updateCurKLine(KLine cur) throws IOException {
        KLine last = curKLine.value();
        //存储的k线为空，或存储的k线的open时间与收到的open时间不同。说明当前k线发生变化，重置状态
        if (last == null || !last.getOpenTime().isEqual(cur.getOpenTime())) {
            curKLine.update(cur);
            isCurSendSignal.update(false);
        }
    }
}

