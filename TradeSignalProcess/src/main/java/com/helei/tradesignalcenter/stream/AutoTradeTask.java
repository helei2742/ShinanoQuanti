package com.helei.tradesignalcenter.stream;

import com.helei.dto.KLine;
import com.helei.dto.SignalGroupKey;
import com.helei.dto.TradeSignal;
import com.helei.tradesignalcenter.stream.c_signal.TradeSignalService;
import com.helei.tradesignalcenter.stream.d_decision.AbstractDecisionMaker;
import com.helei.tradesignalcenter.stream.e_order.AbstractOrderCommitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;

import java.util.List;

@Slf4j
public class AutoTradeTask<T> {


    /**
     * 信号流服务
     */
    private final TradeSignalService tradeSignalService;

    /**
     * 决策服务
     */
    private final AbstractDecisionMaker<T> decisionMaker;

    /**
     * 订单提交器
     */
    private final AbstractOrderCommitter<T> orderCommitter;


    public AutoTradeTask(
            TradeSignalService tradeSignalService,
            AbstractDecisionMaker<T> decisionMaker,
            AbstractOrderCommitter<T> orderCommitter
    ) {
        this.tradeSignalService = tradeSignalService;

        this.decisionMaker = decisionMaker;

        this.orderCommitter = orderCommitter;
    }


    public void execute(String name) throws Exception {

        //1.信号服务
        KeyedStream<Tuple2<SignalGroupKey, List<TradeSignal>>, String> symbolGroupSignalStream = tradeSignalService.getSymbolGroupSignalStream();

//        symbolGroupSignalStream.print();
        //2.决策服务
//        DataStream<T> originOrderStream = symbolGroupSignalStream.process(decisionMaker);

        //3订单提交服务
//        Sink<T> commitSink = orderCommitter.getCommitSink();
//        originOrderStream.sinkTo(commitSink);

        tradeSignalService.getEnv().execute(name);
    }

}




