package com.helei.tradesignalcenter.stream.c_signal;

import com.helei.dto.SignalGroupKey;
import com.helei.dto.indicator.Indicator;
import com.helei.tradesignalcenter.constants.KLineTypeInfo;
import com.helei.dto.KLine;
import com.helei.dto.TradeSignal;
import com.helei.tradesignalcenter.stream.a_klinesource.KLineHisAndRTSource;
import com.helei.tradesignalcenter.stream.b_indicator.IndicatorProcessFunction;
import com.helei.tradesignalcenter.stream.b_indicator.calculater.BaseIndicatorCalculator;
import com.helei.tradesignalcenter.stream.c_signal.maker.AbstractSignalMaker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.*;
import java.util.function.Function;


@Slf4j
@Getter
public class TradeSignalService {
    /**
     * 环境
     */
    private final StreamExecutionEnvironment env;

    /**
     * 信号生成器
     */
    private final List<TradeSignalStreamResolver> resolverList;


    public TradeSignalService(StreamExecutionEnvironment env) {
        this.env = env;
        this.resolverList = new ArrayList<>();
    }


    public static TradeSignalServiceBuilder builder(StreamExecutionEnvironment env) {
        return new TradeSignalServiceBuilder(new TradeSignalService(env));
    }


    /**
     * 添加信号流处理器
     *
     * @param resolver resolver
     */
    public void addTradeSignalStreamResolver(TradeSignalStreamResolver resolver) {
        this.resolverList.add(resolver);
    }


    /**
     * 当前信号流处理器是否为空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return resolverList.isEmpty();
    }


    /**
     * 获取联合的交易信号流，，根据交易对名symbol进行的keyby
     *
     * @return KeyedStream
     */
    public KeyedStream<Tuple2<SignalGroupKey, List<TradeSignal>>, String> getSymbolGroupSignalStream() {
        if (resolverList.isEmpty()) {
            log.error("没有添加信号流处理器");
            throw new IllegalArgumentException("没有添加信号流处理器");
        }

        DataStream<Tuple2<SignalGroupKey, List<TradeSignal>>> combineStream = resolverList.getFirst().makeSignalStream();
//        for (int i = 1; i < resolverList.size(); i++) {
//            combineStream.union(resolverList.get(i).makeSignalStream());
//        }
//
//        return combineStream.keyBy(t2 -> {
//            SignalGroupKey k = t2.getField(0);
//            return k.getSymbol();
//        });

        return null;
    }

    /**
     * 交易信号流处理器
     */
    @Getter
    public static class TradeSignalStreamResolver {
        /**
         * 环境
         */
        private final StreamExecutionEnvironment env;

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


        public TradeSignalStreamResolver(StreamExecutionEnvironment env) {
            this.env = env;

            this.indicatorCalList = new ArrayList<>();

            this.signalMakers = new ArrayList<>();
        }


        /**
         * 开始执行产生信号流
         *
         * @return 信号流
         */
        public DataStream<Tuple2<SignalGroupKey, List<TradeSignal>>> makeSignalStream() {
            if (kLineSource == null) {
                throw new IllegalArgumentException("未添加k线数据源");
            }

            //1. 使用自定义 SourceFunction 生成 K 线数据流
            KeyedStream<KLine, String> kLineStream = env
                    .addSource(kLineSource)
//                    .returns(new KLineTypeInfo())
                    .keyBy(KLine::getStreamKey);

            BaseIndicatorCalculator<?>[] arr = new BaseIndicatorCalculator[indicatorCalList.size()];
            for (int i = 0; i < indicatorCalList.size(); i++) {
                arr[i] = indicatorCalList.get(i);
            }
            // 2.指标处理，串行
            kLineStream = kLineStream
                    .process(new IndicatorProcessFunction(arr))
                    .setParallelism(1)
                    .keyBy(KLine::getStreamKey);

            kLineStream.print();

            if (signalMakers.isEmpty()) {
                throw new IllegalArgumentException("没有信号生成器");
            }

//            //3, 信号处理,并行
//            Iterator<AbstractSignalMaker> signalMakerIterator = signalMakers.iterator();
//
//            DataStream<TradeSignal> signalStream = kLineStream.process(signalMakerIterator.next());
//
//            while (signalMakerIterator.hasNext()) {
//
//                signalStream = signalStream.union(kLineStream.process(signalMakerIterator.next()));
//            }


//            return buildAndSinkGroupStream(kLineStream, signalStream);
            return null;
        }


        /**
         * 根据kline中已完结k线，将signal按照k线进行分组。得到新的流后调用GroupSignalResolver进行sink
         *
         * @param kLineStream  kLineStream
         * @param signalStream signalStream
         * @return 按照k线分组的信号
         */
        private DataStream<Tuple2<SignalGroupKey, List<TradeSignal>>> buildAndSinkGroupStream(KeyedStream<KLine, String> kLineStream, DataStream<TradeSignal> signalStream) {

//            signalStream.print();
            return kLineStream
                    .connect(signalStream.keyBy(TradeSignal::getKlineStreamKey))
                    .process(new SignalSplitResolver(groupWindowRatioOfKLine));
        }
    }

    public static class TradeSignalServiceBuilder {

        private final TradeSignalService tradeSignalService;

        public TradeSignalServiceBuilder(TradeSignalService tradeSignalService) {
            this.tradeSignalService = tradeSignalService;
        }

        public TradeSignalStreamResolverBuilder buildResolver() {
            return new TradeSignalStreamResolverBuilder(tradeSignalService.getEnv(), resolver -> {
                tradeSignalService.addTradeSignalStreamResolver(resolver);
                return this;
            });
        }


        public TradeSignalService build() {
            return tradeSignalService;
        }
    }

    public static class TradeSignalStreamResolverBuilder {

        private final TradeSignalStreamResolver tradeSignalStreamResolver;

        private final Function<TradeSignalStreamResolver, TradeSignalServiceBuilder> addInService;


        TradeSignalStreamResolverBuilder(StreamExecutionEnvironment env, Function<TradeSignalStreamResolver, TradeSignalServiceBuilder> addInService) {
            this.tradeSignalStreamResolver = new TradeSignalStreamResolver(env);
            this.addInService = addInService;
        }


        /**
         * 设置数据源
         *
         * @param kLineSource 数据源
         * @return this
         */
        public TradeSignalStreamResolverBuilder addKLineSource(KLineHisAndRTSource kLineSource) {
            tradeSignalStreamResolver.kLineSource = kLineSource;
            return this;
        }


        /**
         * 设置分组窗口占k线的比例
         *
         * @param ratio ratio
         * @return this
         */
        public TradeSignalStreamResolverBuilder setWindowLengthRationOfKLine(double ratio) {
            tradeSignalStreamResolver.groupWindowRatioOfKLine = ratio;
            return this;
        }


        /**
         * 添加指标计算器
         *
         * @param calculator 指标计算器
         * @return this
         */
        public TradeSignalStreamResolverBuilder addIndicator(BaseIndicatorCalculator<? extends Indicator> calculator) {
            tradeSignalStreamResolver.getIndicatorCalList().add(calculator);
            return this;
        }

        /**
         * 添加信号生成器
         *
         * @param signalMaker 信号生成器
         * @return this
         */
        public TradeSignalStreamResolverBuilder addSignalMaker(AbstractSignalMaker signalMaker) {
            tradeSignalStreamResolver.getSignalMakers().add(signalMaker);
            return this;
        }

        public TradeSignalServiceBuilder addInService() {
            return addInService.apply(tradeSignalStreamResolver);
        }
    }
}

