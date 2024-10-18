package com.helei.tradedatacenter.resolvestream.decision;

import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.indicator.Indicator;
import com.helei.tradedatacenter.resolvestream.indicator.config.IndicatorConfig;
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

import java.util.HashMap;
import java.util.List;


@Slf4j
@Getter
@Setter
public abstract class AbstractDecisionMaker extends KeyedProcessFunction<String, Tuple2<KLine, List<TradeSignal>>, OriginOrder> {

    private final String name;

    /**
     * 存放历史信号
     */
    private MapState<String, Tuple2<KLine, List<TradeSignal>>> historySignalMapState;

    protected AbstractDecisionMaker(String name) {
        this.name = name;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        historySignalMapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("historySignalMapState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<>() {
        })));
    }

    @Override
    public void processElement(Tuple2<KLine, List<TradeSignal>> kLineListTuple2, KeyedProcessFunction<String, Tuple2<KLine, List<TradeSignal>>, OriginOrder>.Context context, Collector<OriginOrder> collector) throws Exception {

        KLine sameSymbolKLine = kLineListTuple2.getField(0);
        String symbol = sameSymbolKLine.getSymbol();
        List<TradeSignal> signals = kLineListTuple2.getField(1);

        if (signals.isEmpty()) {
            log.debug("[{}] - symbol[{}]时间窗口内没有信号", symbol, name);
        } else {
            log.info("[{}] - symbol[{}]当前时间窗口，产生[{}]个信号", name, signals, signals.size());
            OriginOrder order = decisionAndBuilderOrder(symbol, signals, sameSymbolKLine.getIndicators());

            //更新历史信号
            historySignalMapState.put(sameSymbolKLine.getStreamKey(), kLineListTuple2);

            if (order != null) {
                log.info("[{}] - symbol[{}]窗口内信号满足决策下单条件，下单[{}}", name, symbol, order);
                collector.collect(order);
            }
        }
    }

    protected abstract OriginOrder decisionAndBuilderOrder(String symbol, List<TradeSignal> windowSignal, HashMap<IndicatorConfig<? extends Indicator>, Indicator> indicatorMap);

    /**
     * 取历史信号
     *
     * @param kLineStreamKey kLine的key，symbol + interval
     * @return 历史信号
     * @throws Exception
     */
    private Tuple2<KLine, List<TradeSignal>> getHistorySignal(String kLineStreamKey) throws Exception {
        return historySignalMapState.get(kLineStreamKey);
    }
}



