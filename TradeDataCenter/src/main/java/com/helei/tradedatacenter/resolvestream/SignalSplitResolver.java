package com.helei.tradedatacenter.resolvestream;

import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.*;
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


    private ListState<TradeSignal> timebaseSignalListState;

    private ValueState<Tuple2<Long, Long>> timebaseState;

    private final long sendWindowLength;

    private final long allowDelayTime;

    public SignalSplitResolver(Long sendWindowLength, long allowDelayTime) {
        this.sendWindowLength = sendWindowLength;
        this.allowDelayTime = allowDelayTime;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        timebaseSignalListState = getRuntimeContext().getListState(new ListStateDescriptor<>("timebaseSignalListState", TypeInformation.of(TradeSignal.class)));
        timebaseState = getRuntimeContext().getState(new ValueStateDescriptor<>("timebaseState", TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})));
    }


    @Override
    public void processElement1(KLine kLine, KeyedCoProcessFunction<String, KLine, TradeSignal, Tuple2<KLine, List<TradeSignal>>>.Context context, Collector<Tuple2<KLine, List<TradeSignal>>> collector) throws Exception {

        long startTime = getWindowStart(kLine, context.timerService().currentProcessingTime());
        long entTime = startTime + sendWindowLength;


        //获取发送时间窗口内的信号
        List<TradeSignal> signalList = getOrInitSignalListState();

        List<TradeSignal> needSendSignal = new ArrayList<>();


        for (TradeSignal signal : signalList) {//当前k在基线时间内有信号
            //只添加在当前发送窗口的
            if (signal.getCreateTime() >= startTime && signal.getCreateTime() <= entTime) {
                needSendSignal.add(signal);
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
     * @param kLine kLine
     * @param currentTime currentTime
     * @return 窗口的起始时间
     * @throws IOException IOException
     */
    private long getWindowStart(KLine kLine, long currentTime) throws IOException {
        Tuple2<Long, Long> timebase = timebaseState.value();

        if (timebase == null || kLine.isEnd()) {
            timebase = new Tuple2<>(kLine.getOpenTime(), currentTime);
        }
        timebaseState.update(timebase);


        long windowStart = (long)timebase.getField(0) + (currentTime - (long)timebase.getField(1)) / sendWindowLength * sendWindowLength;

        return windowStart - allowDelayTime;
    }

    /**
     * 更新窗口，去除过期的
     * @param signalList signalList
     * @param limitTime limitTime
     * @throws Exception Exception
     */
    private void updateSignalListState(List<TradeSignal> signalList, Long limitTime) throws Exception {
        signalList.removeIf(signal -> signal.getCreateTime() < limitTime);
        timebaseSignalListState.update(signalList);
    }


    /**
     * 取SignalList，如果为空则会初始化
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