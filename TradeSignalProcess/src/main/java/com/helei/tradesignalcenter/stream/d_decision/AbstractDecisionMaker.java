package com.helei.tradesignalcenter.stream.d_decision;

import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.SignalGroupKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;


@Slf4j
@Getter
@Setter
public abstract class AbstractDecisionMaker<T> extends KeyedProcessFunction<String, Tuple2<SignalGroupKey, List<IndicatorSignal>>, T> {

    private final String name;

    /**
     * 存放历史信号
     */
    private MapState<String, Tuple2<SignalGroupKey, List<IndicatorSignal>>> historySignalMapState;

    protected AbstractDecisionMaker(String name) {
        this.name = name;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        historySignalMapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("historySignalMapState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<>() {
        })));
    }

    @Override
    public void processElement(Tuple2<SignalGroupKey, List<IndicatorSignal>> kLineListTuple2, KeyedProcessFunction<String, Tuple2<SignalGroupKey, List<IndicatorSignal>>, T>.Context context, Collector<T> collector) throws Exception {

        SignalGroupKey key = kLineListTuple2.getField(0);
        String symbol = key.getSymbol();
        List<IndicatorSignal> signals = kLineListTuple2.getField(1);

        if (signals.isEmpty()) {
            log.debug("[{}] - symbol[{}]时间窗口内没有信号", symbol, name);
        } else {
            log.debug("[{}] - symbol[{}]当前时间窗口，产生[{}]个信号", name, signals, signals.size());
            T out = decisionAndBuilderOrder(symbol, signals, null);

            //更新历史信号
            historySignalMapState.put(key.getStreamKey(), kLineListTuple2);

            if (out != null) {
                log.info("[{}] - symbol[{}]窗口内信号满足决策下单条件，下单[{}}", name, symbol, out);
                collector.collect(out);
            }
        }
    }

    protected abstract T decisionAndBuilderOrder(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap);

    /**
     * 取历史信号
     * @param kLineStreamKey kLine的key，symbol + interval
     * @return 历史信号
     * @throws Exception
     */
    private Tuple2<SignalGroupKey, List<IndicatorSignal>> getHistorySignal(String kLineStreamKey) throws Exception {
        return historySignalMapState.get(kLineStreamKey);
    }
}



