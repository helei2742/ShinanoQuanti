package com.helei.tradedatacenter.resolvestream;

import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Slf4j
public class SignalSplitResolver extends KeyedBroadcastProcessFunction<String, TradeSignal, KLine, Tuple2<KLine, List<TradeSignal>>> {

    private final MapStateDescriptor<String, KLine> broadcastStateDescriptor;

    private final String CURRENT_KLINE_KEY = "curKLine";

    private ListState<TradeSignal> windowSignal;

    public SignalSplitResolver(MapStateDescriptor<String, KLine> broadcastStateDescriptor) {
        this.broadcastStateDescriptor = broadcastStateDescriptor;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
//                                    curKLineOpenTime = getRuntimeContext().getState(new ValueStateDescriptor<>("curKLineOpenTImeState", BasicTypeInfo.LONG_TYPE_INFO));
        windowSignal = getRuntimeContext().getListState(new ListStateDescriptor<>("tradeSignalWindowState", TypeInformation.of(TradeSignal.class)));
    }

    @Override
    public void processElement(TradeSignal signal, KeyedBroadcastProcessFunction<String, TradeSignal, KLine, Tuple2<KLine, List<TradeSignal>>>.ReadOnlyContext readOnlyContext, Collector<Tuple2<KLine, List<TradeSignal>>> collector) throws Exception {
        // 从广播状态中获取划分规则
        ReadOnlyBroadcastState<String, KLine> broadcastState = readOnlyContext.getBroadcastState(broadcastStateDescriptor);
        KLine curKLine = broadcastState.get(CURRENT_KLINE_KEY);

        //窗口的写入
        Iterable<TradeSignal> windowState = windowSignal.get();

        long curOpenTime = curKLine.getOpenTime();
        long signalTime = signal.getKLine().getOpenTime();

        if (signalTime == curOpenTime) { // 是当前k线
            windowSignal.add(signal);
        } else {
            Iterator<TradeSignal> iterable = windowState.iterator();

            List<TradeSignal> curWindow = new ArrayList<>();
            while (iterable.hasNext()) {
                curWindow.add(iterable.next());
            }

            collector.collect(Tuple2.of(curKLine, curWindow));
            windowSignal.update(new ArrayList<>());

            log.warn("信号时间[{}]不在当前k线时间[{}]内, k时间已过期", Date.from(Instant.ofEpochMilli(signalTime)), Date.from(Instant.ofEpochMilli(curOpenTime)));
        }
    }

    @Override
    public void processBroadcastElement(KLine kLine, KeyedBroadcastProcessFunction<String, TradeSignal, KLine, Tuple2<KLine, List<TradeSignal>>>.Context context, Collector<Tuple2<KLine, List<TradeSignal>>> collector) throws Exception {
        if (!kLine.isEnd()) return;
        // 来到新的k线
        //更新当前k状态
        BroadcastState<String, KLine> broadcastState = context.getBroadcastState(broadcastStateDescriptor);
        broadcastState.put(CURRENT_KLINE_KEY, kLine);
    }
}
