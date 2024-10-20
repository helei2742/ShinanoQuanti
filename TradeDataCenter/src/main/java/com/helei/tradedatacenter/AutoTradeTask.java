package com.helei.tradedatacenter;

import com.helei.binanceapi.dto.order.BaseOrder;
import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;

import java.util.List;

@Slf4j
public class AutoTradeTask {


    /**
     * 信号流服务
     */
    private final TradeSignalService tradeSignalService;

    /**
     * 决策服务
     */
    private final DecisionMakerService decisionMakerService;

    /**
     * 订单提交器
     */
    private final OrderCommitService orderCommitService;


    public AutoTradeTask(
            TradeSignalService tradeSignalService,
            DecisionMakerService decisionMakerService,
            OrderCommitService orderCommitService
    ) {
        this.tradeSignalService = tradeSignalService;

        this.decisionMakerService = decisionMakerService;

        this.orderCommitService = orderCommitService;
    }


    public void execute(String name) throws Exception {

        //1.信号服务
        KeyedStream<Tuple2<KLine, List<TradeSignal>>, String> symbolGroupSignalStream = tradeSignalService.getSymbolGroupSignalStream();

        //2.决策服务
        DataStream<OriginOrder> originOrderStream = decisionMakerService.decision(symbolGroupSignalStream);

        //3订单提交服务
        DataStream<BaseOrder> commitedOrderStream = orderCommitService.commitOrder(originOrderStream);

        //4已提交订单入库
        //TODO
        commitedOrderStream.print();

        tradeSignalService.getEnv().execute(name);
    }

}




