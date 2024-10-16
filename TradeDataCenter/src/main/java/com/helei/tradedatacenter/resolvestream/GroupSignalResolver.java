package com.helei.tradedatacenter.resolvestream;

import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.util.List;

public abstract class GroupSignalResolver extends RichSinkFunction<Tuple2<KLine, List<TradeSignal>>> {


    @Override
    public void invoke(Tuple2<KLine, List<TradeSignal>> value, Context context) throws Exception {
        super.invoke(value, context);
    }
}
