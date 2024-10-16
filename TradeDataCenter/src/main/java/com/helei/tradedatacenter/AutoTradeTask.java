package com.helei.tradedatacenter;

import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.decision.AbstractDecisionMaker;
import com.helei.tradedatacenter.resolvestream.order.AbstractOrderCommitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class AutoTradeTask {


    /**
     * 信号流服务
     */
    private final TradeSignalService tradeSignalService;

    /**
     * 决策器
     */
    private final List<AbstractDecisionMaker> decisionMakers;

    /**
     * 订单提交器
     */
    private final List<AbstractOrderCommitter> orderCommiters;



    public AutoTradeTask(TradeSignalService tradeSignalService) {
        this.tradeSignalService = tradeSignalService;

        this.decisionMakers = new ArrayList<>();

        this.orderCommiters = new ArrayList<>();
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
        if (decisionMakers.isEmpty()) {
            throw new IllegalArgumentException("没有添加决策器");
        }

        KeyedStream<TradeSignal, String> signalStream = tradeSignalService.getCombineTradeSignalStream();

        signalStream.print();
        DataStream<List<TradeSignal>> windowSignal = signalStream
                .timeWindow(Time.of(Duration.ofMinutes(1)), Time.of(Duration.ofSeconds(10)))
                .aggregate(new AggregateFunction<TradeSignal, List<TradeSignal>, List<TradeSignal>>() {
                    @Override
                    public List<TradeSignal> createAccumulator() {
                        return new ArrayList<>();
                    }

                    @Override
                    public List<TradeSignal> add(TradeSignal signal, List<TradeSignal> tradeSignals) {
                        tradeSignals.add(signal);
                        return tradeSignals;
                    }

                    @Override
                    public List<TradeSignal> getResult(List<TradeSignal> tradeSignals) {
                        return tradeSignals;
                    }

                    @Override
                    public List<TradeSignal> merge(List<TradeSignal> tradeSignals, List<TradeSignal> acc1) {
                        tradeSignals.addAll(acc1);
                        return tradeSignals;
                    }
                });

        //4、决策器，
        Iterator<AbstractDecisionMaker> decisionMakerIterator = decisionMakers.iterator();

        KeyedStream<OriginOrder, String> orderStream = windowSignal.process(decisionMakerIterator.next()).keyBy(OriginOrder::getSymbol);

        while (decisionMakerIterator.hasNext()) {
            orderStream.union(windowSignal.process(decisionMakerIterator.next()));
        }
        orderStream.print();
        //最后将决策走到订单提交器
        for (AbstractOrderCommitter orderCommitter : orderCommiters) {
            orderStream.addSink(orderCommitter);
        }

        tradeSignalService.getEnv().execute(name);
    }

}
