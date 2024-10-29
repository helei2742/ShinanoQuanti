package com.helei.tradesignalcenter.stream.c_indicator_signal;

import com.helei.dto.IndicatorSignal;
import com.helei.dto.SignalGroupKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.*;


@Slf4j
@Getter
public class IndicatorSignalService {
    /**
     * 环境
     */
    private final StreamExecutionEnvironment env;

    /**
     * 信号生成器
     */
    private final List<IndicatorSignalStreamProcessor> processorList;


    public IndicatorSignalService(StreamExecutionEnvironment env) {
        this.env = env;
        this.processorList = new ArrayList<>();
    }


    /**
     * 添加信号流处理器
     *
     * @param processor processor
     */
    public void addIndicatorSignalStreamProcessor(IndicatorSignalStreamProcessor processor) {
        this.processorList.add(processor);
    }


    /**
     * 当前信号流处理器是否为空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return processorList.isEmpty();
    }


    /**
     * 获取联合的交易信号流，，根据交易对名symbol进行的keyby
     *
     * @return KeyedStream
     */
    public KeyedStream<Tuple2<SignalGroupKey, List<IndicatorSignal>>, String> getSymbolGroupSignalStream() {
        if (processorList.isEmpty()) {
            log.error("没有添加信号流处理器");
            throw new IllegalArgumentException("没有添加信号流处理器");
        }

        DataStream<Tuple2<SignalGroupKey, List<IndicatorSignal>>> combineStream = processorList.getFirst().makeIndicatorSignalStream();
        for (int i = 1; i < processorList.size(); i++) {
            combineStream.union(processorList.get(i).makeIndicatorSignalStream());
        }

        return combineStream.keyBy(t2 -> {
            SignalGroupKey k = t2.getField(0);
            return k.getSymbol();
        });
    }


    public static TradeSignalServiceBuilder builder(StreamExecutionEnvironment env) {
        return new TradeSignalServiceBuilder(env);
    }

    public static class TradeSignalServiceBuilder {

        private final IndicatorSignalService indicatorSignalService;

        public TradeSignalServiceBuilder(StreamExecutionEnvironment env) {
            this.indicatorSignalService = new IndicatorSignalService(env);
        }

        public TradeSignalServiceBuilder addIndicatorSignalProcessor(IndicatorSignalStreamProcessor processor) {
            processor.setEnv(indicatorSignalService.getEnv());
            indicatorSignalService.addIndicatorSignalStreamProcessor(processor);
            return this;
        }


        public IndicatorSignalService build() {
            return indicatorSignalService;
        }
    }

}
