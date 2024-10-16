package com.helei.tradedatacenter;

        import com.helei.tradedatacenter.datasource.BaseKLineSource;
        import com.helei.tradedatacenter.entity.KLine;
        import com.helei.tradedatacenter.entity.TradeSignal;
        import com.helei.tradedatacenter.resolvestream.indicator.Indicator;
        import com.helei.tradedatacenter.resolvestream.indicator.calculater.BaseIndicatorCalculator;
        import com.helei.tradedatacenter.resolvestream.signal.AbstractSignalMaker;
        import lombok.Getter;
        import lombok.extern.slf4j.Slf4j;
        import org.apache.flink.streaming.api.datastream.DataStream;
        import org.apache.flink.streaming.api.datastream.KeyedStream;
        import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

        import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.List;
        import java.util.function.Function;


@Slf4j
@Getter
public class TradeSignalService {
    /**
     * 环境
     */
    private final StreamExecutionEnvironment env;


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
     * @param resolver resolver
     */
    public void addTradeSignalStreamResolver(TradeSignalStreamResolver resolver) {
        this.resolverList.add(resolver);
    }


    /**
     * 当前信号流处理器是否为空
     * @return boolean
     */
    public boolean isEmpty() {
        return resolverList.isEmpty();
    }


    /**
     * 获取联合的交易信号流，，根据交易对名symbol进行的keyby
     * @return KeyedStream
     */
    public KeyedStream<TradeSignal, String> getCombineTradeSignalStream() {
        if (resolverList.isEmpty()) {
            log.error("没有添加信号流处理器");
            throw new IllegalArgumentException("没有添加信号流处理器");
        }

        DataStream<TradeSignal> combineStream = resolverList.get(0).makeSignalStream();
        for (int i = 1; i < resolverList.size(); i++) {
            combineStream.union(resolverList.get(i).makeSignalStream());
        }

        return combineStream.keyBy(TradeSignal::getStreamKey);
    }



    @Getter
    public static class TradeSignalStreamResolver {
        /**
         * 环境
         */
        private final StreamExecutionEnvironment env;

        /**
         * k线数据源
         */
        private BaseKLineSource kLineSource;

        /**
         * 指标计算器
         */
        private final List<BaseIndicatorCalculator<? extends Indicator>> indicatorCalList;

        /**
         * 信号处理器
         */
        private final List<AbstractSignalMaker> signalMakers;


        public TradeSignalStreamResolver(StreamExecutionEnvironment env) {
            this.env = env;

            this.indicatorCalList = new ArrayList<>();

            this.signalMakers = new ArrayList<>();
        }


        public DataStream<TradeSignal> makeSignalStream() {
            if (kLineSource == null) {
                throw new IllegalArgumentException("未添加k线数据源");
            }

            //1. 使用自定义 SourceFunction 生成 K 线数据流
            KeyedStream<KLine, String> kLineStream = env.addSource(kLineSource).keyBy(KLine::getStreamKey);

//            kLineStream.print();
            // 2.指标处理，串行
            for (BaseIndicatorCalculator<? extends Indicator> calculator : indicatorCalList) {
                kLineStream = kLineStream.process(calculator).keyBy(KLine::getStreamKey);
            }
//            kLineStream.print();
            if (signalMakers.isEmpty()) {
                throw new IllegalArgumentException("没有信号生成器");
            }

            //3, 信号处理,并行
            Iterator<AbstractSignalMaker> signalMakerIterator = signalMakers.iterator();

            DataStream<TradeSignal> signalStream = kLineStream.process(signalMakerIterator.next());

            while (signalMakerIterator.hasNext()) {

                signalStream.union(kLineStream.process(signalMakerIterator.next()));
            }


            return signalStream;
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
        public TradeSignalStreamResolverBuilder addKLineSource(BaseKLineSource kLineSource) {
            tradeSignalStreamResolver.kLineSource = kLineSource;
            return this;
        }

        /**
         * 添加指标计算器
         *
         * @param calculator 指标计算器
         * @param <T>        指标类型
         * @return this
         */
        public <T extends Indicator> TradeSignalStreamResolverBuilder addIndicator(BaseIndicatorCalculator<T> calculator) {
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