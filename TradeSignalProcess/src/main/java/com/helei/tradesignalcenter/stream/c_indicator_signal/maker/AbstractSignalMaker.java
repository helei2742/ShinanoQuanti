package com.helei.tradesignalcenter.stream.c_indicator_signal.maker;

import cn.hutool.core.util.BooleanUtil;
import com.helei.dto.IndicatorSignal;
import com.helei.dto.KLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
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
public abstract class AbstractSignalMaker extends KeyedProcessFunction<String, KLine, IndicatorSignal> {

    /**
     * 是否是一条k线只发出一个信号
     */
    private final boolean isAKLineSendOneSignal;


    /**
     * 时间基线， 第一个为收到第一个k或下一个k的openTime， 第二个为对应的processTime
     */
    protected ValueState<Tuple2<Long, Long>> timebaseState;

    /**
     * 当前k线，就是buildSignal(kline) 参数kline同意openTime的k线
     */
    protected ValueState<KLine> curKLine;

    /**
     * 前一条已完结的k线
     */
    protected ValueState<KLine> lastHistoryKLine;

    /**
     * 当前是否发出过信号
     */
    private ValueState<Boolean> isCurSendSignal;


    protected AbstractSignalMaker(boolean isAKLineSendOneSignal) {
        this.isAKLineSendOneSignal = isAKLineSendOneSignal;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        timebaseState = getRuntimeContext().getState(new ValueStateDescriptor<>("timebaseState", TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {
        })));
        curKLine = getRuntimeContext().getState(new ValueStateDescriptor<>("currentKLine", TypeInformation.of(KLine.class)));
        lastHistoryKLine = getRuntimeContext().getState(new ValueStateDescriptor<>("lastHistoryKLine", TypeInformation.of(KLine.class)));
        isCurSendSignal = getRuntimeContext().getState(new ValueStateDescriptor<>("isCurSendSignal", Boolean.class));

        this.onOpen(openContext);
    }

    @Override
    public void processElement(KLine kLine, KeyedProcessFunction<String, KLine, IndicatorSignal>.Context context, Collector<IndicatorSignal> collector) throws Exception {

        //更新历史k，实时k
        updateCurKLine(kLine, context.timerService().currentProcessingTime());

        try {

            IndicatorSignal indicatorSignal;

            if (BooleanUtil.isTrue(kLine.isEnd())) { //历史k线发出的信号打上标识
                indicatorSignal = resolveHistoryKLine(kLine, context.timerService());
            } else {
                indicatorSignal = resolveRealTimeKLine(kLine, context.timerService());
            }
            if (indicatorSignal == null) return;

            setSignalCreateTIme(indicatorSignal, context.timerService().currentProcessingTime());

            indicatorSignal.setKLine(kLine);

//            if (isAKLineSendOneSignal && BooleanUtil.isTrue(isCurSendSignal.value())) {
//                //当前k线发送过信号
//                log.debug("this kLine sent signal, cancel send this time");
//            } else {
//                isCurSendSignal.update(true);
            collector.collect(indicatorSignal);

//                log.debug("signal maker send a signal: [{}]", tradeSignal);
//            }
        } catch (Exception e) {
            log.error("build signal error", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onTimer(long timestamp, KeyedProcessFunction<String, KLine, IndicatorSignal>.OnTimerContext ctx, Collector<IndicatorSignal> out) throws Exception {
        IndicatorSignal indicatorSignal = onTimerInvoke();
        if (indicatorSignal != null) {
            log.info("signal maker send a timer signal: [{}]", indicatorSignal);

            indicatorSignal.setCreateTime(indicatorSignal.getKLine().isEnd() ? indicatorSignal.getKLine().getCloseTime() : ctx.timerService().currentProcessingTime());
            out.collect(indicatorSignal);
        }
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
     * @param kLine        已完结的k线数据
     * @param timerService timerService
     */
    protected abstract IndicatorSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception;

    /**
     * 产生信号
     *
     * @param kLine        实时推送的k线数据
     * @param timerService timerService
     * @return 交易信号
     */
    protected abstract IndicatorSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception;


    /**
     * 产出定时信号，要触发，先要调用 context.timerService().registerProcessingTimeTimer(timer);
     *
     * @return TradeSignal
     * @throws IOException IOException
     */
    protected IndicatorSignal onTimerInvoke() throws Exception {
        return null;
    }

    /**
     * 更新当前k线，如果成功更新，还要将isSendSignal设置为false
     *
     * @param cur cur
     * @throws IOException IOException
     */
    private void updateCurKLine(KLine cur, long currentTime) throws IOException {
        Tuple2<Long, Long> timebase = timebaseState.value();

        if (timebase == null || cur.isEnd()) {
            timebase = new Tuple2<>(cur.getOpenTime(), currentTime);
        }
        timebaseState.update(timebase);

        KLine last = curKLine.value();
        //存储的k线为空，或存储的k线的open时间与收到的open时间不同。说明当前k线发生变化，重置状态
        if (last == null || last.getCloseTime() < cur.getOpenTime()) {
            isCurSendSignal.update(false);
        }
        if (cur.isEnd()) {
            lastHistoryKLine.update(cur);
        }

        curKLine.update(cur);
    }

    /**
     * 设置信号的创建时间
     *
     * @param currentTime 当前时间
     * @return 创建时间
     */
    public long setSignalCreateTIme(IndicatorSignal indicatorSignal, long currentTime) throws IOException {
        Tuple2<Long, Long> timebase = timebaseState.value();
        if (timebase == null) {
            log.error("获取timebase错误，当前timebase不应为null");
            return -1;
        }
        long createTime = (long) timebase.getField(0) + (currentTime - (long) timebase.getField(1));
        //设置创建时间, 历史k线得到的信号，时间为这个k的结束时间
        indicatorSignal.setCreateTime(createTime);

        return createTime;
    }
}
