package com.helei.tradesignalcenter.resolvestream.c_signal;

import com.helei.dto.KLine;
import com.helei.dto.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.*;


/**
 * 信号分片器，将信号按照K线进行分组
 */
@Slf4j
public class SignalSplitResolver extends KeyedCoProcessFunction<String, KLine, TradeSignal, Tuple2<KLine, List<TradeSignal>>> {

    /**
     * 发送窗口占k线的比例
     */
    private final double groupWindowRatioOfKLine;

    /**
     * 存储当前收到的信号
     */
    private ListState<TradeSignal> timebaseSignalListState;

    /**
     * 时间基线
     * index=0，为作为时间基线的k线的openTime
     * index=1，为创建这个基线时的系统ProcessTime
     */
    private ValueState<Tuple2<Long, Long>> timebaseState;

    /**
     * 发送窗口的开始时间
     */
    private ValueState<Long> sendWindowStartState;

    /**
     * 当前窗口的开始时间
     */
    private ValueState<Long> windowStartState;

    /**
     * 窗口长度
     */
    private ValueState<Long> windowLengthState;

    public SignalSplitResolver(double groupWindowRatioOfKLine) {
        this.groupWindowRatioOfKLine = groupWindowRatioOfKLine;
    }


    @Override
    public void open(Configuration parameters) throws Exception {
        timebaseSignalListState = getRuntimeContext().getListState(new ListStateDescriptor<>("timebaseSignalListState", TypeInformation.of(TradeSignal.class)));
        timebaseState = getRuntimeContext().getState(new ValueStateDescriptor<>("timebaseState", TypeInformation.of(new TypeHint<>() {})));
        sendWindowStartState = getRuntimeContext().getState(new ValueStateDescriptor<>("sendWindowStartState", BasicTypeInfo.LONG_TYPE_INFO));
        windowStartState = getRuntimeContext().getState(new ValueStateDescriptor<>("windowStartState", BasicTypeInfo.LONG_TYPE_INFO));
    }


    @Override
    public void processElement1(KLine kLine, KeyedCoProcessFunction<String, KLine, TradeSignal, Tuple2<KLine, List<TradeSignal>>>.Context context, Collector<Tuple2<KLine, List<TradeSignal>>> collector) throws Exception {

        long startTime = getWindowStart(kLine, context.timerService().currentProcessingTime());


        //获取发送时间窗口内的信号
        List<TradeSignal> signalList = getOrInitSignalListState();

        List<TradeSignal> needSendSignal = new ArrayList<>();


        Long sendWindowStart = sendWindowStartState.value();
        if (sendWindowStart != null) {
            for (TradeSignal signal : signalList) {//当前k在基线时间内有信号
                //只添加在当前发送窗口的
                if (signal.getCreateTime() >= sendWindowStart && signal.getCreateTime() <= sendWindowStart + windowLengthState.value()) {
                    needSendSignal.add(signal);
                }
            }
        }

        //发送流
        collector.collect(new Tuple2<>(kLine, needSendSignal));

        //更新发送窗口
        updateSignalListState(signalList, startTime);
    }


    @Override
    public void processElement2(TradeSignal signal, KeyedCoProcessFunction<String, KLine, TradeSignal, Tuple2<KLine, List<TradeSignal>>>.Context context, Collector<Tuple2<KLine, List<TradeSignal>>> collector) throws Exception {
        List<TradeSignal> signalList = getOrInitSignalListState();
        if (signal.getCreateTime() == null) {
            log.warn("信号没有创建时间，将自动丢弃.[{}]", signal);
            return;
        }
        signalList.add(signal);
        timebaseSignalListState.update(signalList);
    }


    /**
     * 获取当前窗口的起始时间
     *
     * @param kLine       kLine
     * @param currentTime currentTime
     * @return 当前窗口的起始位置
     * @throws IOException IOException
     */
    private Long getWindowStart(KLine kLine, long currentTime) throws IOException {
        //1. 获取窗口长度
        Long sendWindowLength = windowLengthState.value();
        if (sendWindowLength == null) {
            sendWindowLength = (long) (kLine.getKLineInterval().getSecond() * groupWindowRatioOfKLine * 1000);
            windowLengthState.update(sendWindowLength);
        }

        //2. 获取时间基线，当前K线open时间和当前时间
        Tuple2<Long, Long> timebase = timebaseState.value();
        if (timebase == null || kLine.isEnd()) {
            timebase = new Tuple2<>(kLine.getOpenTime(), currentTime);
        }
        timebaseState.update(timebase);

        //3. 确定窗口的开始和发送窗口的开始
        Long lastWindowStart = windowStartState.value();
        Long windowStart = (Long) timebase.getField(0) + (currentTime - (Long) timebase.getField(1)) / sendWindowLength * sendWindowLength;
        windowStartState.update(windowStart);

        if (lastWindowStart == null) {
            lastWindowStart = windowStart;
        }

        //4.根据窗口其实位置是否发生变化，设置是否能够发送信号的状态
        if (!windowStart.equals(lastWindowStart)) {
            sendWindowStartState.update(lastWindowStart);
        } else {
            sendWindowStartState.update(null);
        }

        return windowStart;
    }

    /**
     * 更新窗口，去除过期的
     *
     * @param signalList signalList
     * @param limitTime  limitTime
     * @throws Exception Exception
     */
    private void updateSignalListState(List<TradeSignal> signalList, Long limitTime) throws Exception {
        signalList.removeIf(signal -> signal.getCreateTime() < limitTime);
        timebaseSignalListState.update(signalList);
    }


    /**
     * 取SignalList，如果为空则会初始化
     *
     * @return SignalList
     * @throws Exception SignalList
     */
    private List<TradeSignal> getOrInitSignalListState() throws Exception {
        List<TradeSignal> signals = new ArrayList<>();
        Iterable<TradeSignal> iterable = timebaseSignalListState.get();

        if (iterable == null) {
            timebaseSignalListState.update(signals);
            return signals;
        }

        iterable.forEach(signals::add);
        signals.sort(Comparator.comparing(TradeSignal::getCreateTime));
        return signals;
    }
}
