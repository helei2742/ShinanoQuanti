
package com.helei.tradedatacenter;

import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.decision.AbstractDecisionMaker;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;


import java.util.List;

/**
 * 决策服务
 */
public class DecisionMakerService {

    private final AbstractDecisionMaker decisionMaker;

    public DecisionMakerService(AbstractDecisionMaker decisionMaker) {
        this.decisionMaker = decisionMaker;
    }


    public DataStream<OriginOrder> decision(KeyedStream<Tuple2<KLine, List<TradeSignal>>, String> symbolGroupSignalStream) {
        return symbolGroupSignalStream.process(decisionMaker);
    }

}

