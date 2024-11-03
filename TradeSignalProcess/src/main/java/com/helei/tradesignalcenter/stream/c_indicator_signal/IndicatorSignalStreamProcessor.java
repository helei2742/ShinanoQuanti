package com.helei.tradesignalcenter.stream.c_indicator_signal;

import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.KLine;
import com.helei.dto.trade.SignalGroupKey;
import com.helei.dto.indicator.Indicator;
import com.helei.tradesignalcenter.stream.a_klinesource.KLineHisAndRTSource;
import com.helei.tradesignalcenter.stream.b_indicator.IndicatorProcessFunction;
import com.helei.tradesignalcenter.stream.b_indicator.calculater.BaseIndicatorCalculator;
import com.helei.tradesignalcenter.stream.c_indicator_signal.maker.AbstractSignalMaker;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 指标信号流处理器
 */
@Getter
public class IndicatorSignalStreamProcessor {
    /**
     * 环境
     */
    @Setter
    private StreamExecutionEnvironment env;

    /**
     * k线数据源
     */
    private KLineHisAndRTSource kLineSource;

    /**
     * 指标计算器
     */
    private final List<BaseIndicatorCalculator<? extends Indicator>> indicatorCalList;

    /**
     * 信号处理器
     */
    private final List<AbstractSignalMaker> signalMakers;

    /**
     * 分组窗口占k线时间区间的比例
     */
    private double groupWindowRatioOfKLine = 0.2;


    public IndicatorSignalStreamProcessor() {

        this.indicatorCalList = new ArrayList<>();

        this.signalMakers = new ArrayList<>();
    }




    /**
     * 开始执行产生信号流
     *
     * @return 信号流
     */
    public DataStream<Tuple2<SignalGroupKey, List<IndicatorSignal>>> makeIndicatorSignalStream() {
        if (kLineSource == null) {
            throw new IllegalArgumentException("未添加k线数据源");
        }

        //1. 使用自定义 SourceFunction 生成 K 线数据流
        KeyedStream<KLine, String> kLineStream = env
                .addSource(kLineSource)
                .keyBy(KLine::getStreamKey);

//            kLineStream.print();

        BaseIndicatorCalculator<?>[] arr = new BaseIndicatorCalculator[indicatorCalList.size()];
        for (int i = 0; i < indicatorCalList.size(); i++) {
            arr[i] = indicatorCalList.get(i);
        }
        // 2.指标处理，串行
        kLineStream = kLineStream
                .process(new IndicatorProcessFunction(arr))
                .setParallelism(1)
                .keyBy(KLine::getStreamKey);

//            kLineStream.print();

        if (signalMakers.isEmpty()) {
            throw new IllegalArgumentException("没有信号生成器");
        }

        //3, 信号处理,并行
        Iterator<AbstractSignalMaker> signalMakerIterator = signalMakers.iterator();

        DataStream<IndicatorSignal> signalStream = kLineStream.process(signalMakerIterator.next());

        while (signalMakerIterator.hasNext()) {

            signalStream = signalStream.union(kLineStream.process(signalMakerIterator.next()));
        }


        return groupIndicatorSignalStreamBySymbol(kLineStream, signalStream);
    }


    /**
     * 根据kline中已完结k线，将signal按照k线进行分组
     *
     * @param kLineStream  kLineStream
     * @param signalStream signalStream
     * @return 按照k线分组的信号
     */
    private DataStream<Tuple2<SignalGroupKey, List<IndicatorSignal>>> groupIndicatorSignalStreamBySymbol(KeyedStream<KLine, String> kLineStream, DataStream<IndicatorSignal> signalStream) {
        return kLineStream
                .connect(signalStream.keyBy(IndicatorSignal::getKlineStreamKey))
                .process(new SignalSplitResolver(groupWindowRatioOfKLine));
    }


    public static IndicatorSignalStreamResolverBuilder builder() {
        return new IndicatorSignalStreamResolverBuilder();
    }


    public static class IndicatorSignalStreamResolverBuilder {

        private final IndicatorSignalStreamProcessor indicatorSignalStreamProcessor;


        public IndicatorSignalStreamResolverBuilder() {
            this.indicatorSignalStreamProcessor = new IndicatorSignalStreamProcessor();
        }


        /**
         * 设置数据源
         *
         * @param kLineSource 数据源
         * @return this
         */
        public IndicatorSignalStreamResolverBuilder addKLineSource(KLineHisAndRTSource kLineSource) {
            indicatorSignalStreamProcessor.kLineSource = kLineSource;
            return this;
        }


        /**
         * 设置分组窗口占k线的比例
         *
         * @param ratio ratio
         * @return this
         */
        public IndicatorSignalStreamResolverBuilder setWindowLengthRationOfKLine(double ratio) {
            indicatorSignalStreamProcessor.groupWindowRatioOfKLine = ratio;
            return this;
        }


        /**
         * 添加指标计算器
         *
         * @param calculator 指标计算器
         * @return this
         */
        public IndicatorSignalStreamResolverBuilder addIndicator(BaseIndicatorCalculator<? extends Indicator> calculator) {
            indicatorSignalStreamProcessor.getIndicatorCalList().add(calculator);
            return this;
        }

        /**
         * 添加信号生成器
         *
         * @param signalMaker 信号生成器
         * @return this
         */
        public IndicatorSignalStreamResolverBuilder addSignalMaker(AbstractSignalMaker signalMaker) {
            indicatorSignalStreamProcessor.getSignalMakers().add(signalMaker);
            return this;
        }

        public IndicatorSignalStreamProcessor build() {
            return indicatorSignalStreamProcessor;
        }
    }
}
