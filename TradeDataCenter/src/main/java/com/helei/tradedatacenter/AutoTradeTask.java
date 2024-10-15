package com.helei.tradedatacenter;

import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.tradedatacenter.datasource.BaseKLineSource;
import com.helei.tradedatacenter.resolvestream.decision.AbstractDecisionMaker;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.indicator.Indicator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.BaseIndicatorCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.calculater.PSTCalculator;
import com.helei.tradedatacenter.resolvestream.indicator.config.PSTConfig;
import com.helei.tradedatacenter.resolvestream.order.AbstractOrderCommitter;
import com.helei.tradedatacenter.resolvestream.signal.AbstractSignalMaker;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AutoTradeTask {

    /**
     * 环境
     */
    private final StreamExecutionEnvironment env;

    /**
     * k线数据源
     */
    private final BaseKLineSource memoryKLineSource;

    /**
     * PST设置
     */
    private PSTConfig pstConfig;

    /**
     * 指标计算器
     */
    private final List<BaseIndicatorCalculator<? extends Indicator>> indicatorCalList;

    /**
     * 信号处理器
     */
    private final List<AbstractSignalMaker> signalMakers;

    /**
     * 决策器
     */
    private final List<AbstractDecisionMaker> decisionMakers;

    /**
     * 订单提交器
     */
    private final List<AbstractOrderCommitter> orderCommiters;


    public AutoTradeTask(StreamExecutionEnvironment env, BaseKLineSource memoryKLineSource) {
        // 设置 Flink 流环境
        this.env = env;

        this.memoryKLineSource = memoryKLineSource;

        this.indicatorCalList = new ArrayList<>();

        this.signalMakers = new ArrayList<>();

        this.decisionMakers = new ArrayList<>();

        this.orderCommiters = new ArrayList<>();
    }

    /**
     * 添加指标计算器
     *
     * @param calculator 指标计算器
     * @param <T>        指标类型
     * @return this
     */
    public <T extends Indicator> AutoTradeTask addIndicator(BaseIndicatorCalculator<T> calculator) {
        indicatorCalList.add(calculator);
        return this;
    }

    /**
     * 添加信号生成器
     *
     * @param signalMaker 信号生成器
     * @return this
     */
    public AutoTradeTask addSignalMaker(AbstractSignalMaker signalMaker) {
        this.signalMakers.add(signalMaker);
        return this;
    }

    /**
     * 添加决策器
     *
     * @param decisionMaker 决策器
     * @return this
     */
    public AutoTradeTask addDecisionMaker(AbstractDecisionMaker decisionMaker) {
        this.decisionMakers.add(decisionMaker);
        return this;
    }


    /**
     * 添加订单提交器
     *
     * @param orderCommiter 订单提交器
     * @return this
     */
    public AutoTradeTask addOrderCommiter(AbstractOrderCommitter orderCommiter) {
        this.orderCommiters.add(orderCommiter);
        return this;
    }

    public void execute(String name) throws Exception {
        //1. 使用自定义 SourceFunction 生成 K 线数据流
        KeyedStream<KLine, String> kLineStream = env.addSource(memoryKLineSource).keyBy(KLine::getStreamKey);

        // 2.指标处理，串行
        for (BaseIndicatorCalculator<? extends Indicator> calculator : indicatorCalList) {
            kLineStream = kLineStream.process(calculator).keyBy(KLine::getStreamKey);
        }

        if (signalMakers.isEmpty()) {
            throw new IllegalArgumentException("no signal maker");
        }

        //3, 信号处理,并行
        Iterator<AbstractSignalMaker> signalMakerIterator = signalMakers.iterator();

        KeyedStream<TradeSignal, String> signalStream = kLineStream.process(signalMakerIterator.next()).keyBy(signal -> signal.getKLine().getStreamKey());

        while (signalMakerIterator.hasNext()) {
            signalStream.union(kLineStream.process(signalMakerIterator.next()));
        }


        if (decisionMakers.isEmpty()) {
            throw new IllegalArgumentException("no decision maker");
        }

        //4、决策器，
        Iterator<AbstractDecisionMaker> decisionMakerIterator = decisionMakers.iterator();

        KeyedStream<BaseOrder, String> orderStream = signalStream.process(decisionMakerIterator.next()).keyBy(BaseOrder::getSymbol);

        while (decisionMakerIterator.hasNext()) {
            orderStream.union(signalStream.process(decisionMakerIterator.next()));
        }


        //最后将决策走到订单提交器
        for (AbstractOrderCommitter orderCommitter : orderCommiters) {
            orderStream.addSink(orderCommitter);
        }

        env.execute(name);
    }

}
