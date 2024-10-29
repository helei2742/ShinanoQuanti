package com.helei.tradesignalcenter.stream;

import com.helei.dto.IndicatorSignal;
import com.helei.dto.SignalGroupKey;
import com.helei.tradesignalcenter.stream.c_indicator_signal.IndicatorSignalService;
import com.helei.tradesignalcenter.stream.d_decision.AbstractDecisionMaker;
import com.helei.tradesignalcenter.stream.e_trade_signal.AbstractTradeSignalCommitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;

import java.util.List;

@Slf4j
public class TradeSignalBuildTask<T> {


    /**
     * 信号流服务
     */
    private final IndicatorSignalService indicatorSignalService;

    /**
     * 决策服务
     */
    private final AbstractDecisionMaker<T> decisionMaker;

    /**
     * 订单提交器
     */
    private final AbstractTradeSignalCommitter<T> orderCommitter;


    public TradeSignalBuildTask(
            IndicatorSignalService indicatorSignalService,
            AbstractDecisionMaker<T> decisionMaker,
            AbstractTradeSignalCommitter<T> orderCommitter
    ) {
        this.indicatorSignalService = indicatorSignalService;

        this.decisionMaker = decisionMaker;

        this.orderCommitter = orderCommitter;
    }


    public void execute(String name) throws Exception {

        //1.信号服务
        KeyedStream<Tuple2<SignalGroupKey, List<IndicatorSignal>>, String> symbolGroupSignalStream = indicatorSignalService.getSymbolGroupSignalStream();

        symbolGroupSignalStream.print();
        //2.决策服务
        DataStream<T> originOrderStream = symbolGroupSignalStream.process(decisionMaker);

//        originOrderStream.print();
        //3订单提交服务
        Sink<T> commitSink = orderCommitter.getCommitSink();
        originOrderStream.sinkTo(commitSink);

        indicatorSignalService.getEnv().execute(name);
    }

}
