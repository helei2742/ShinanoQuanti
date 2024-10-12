
package com.helei.tradedatacenter;

import com.helei.tradedatacenter.datasource.BaseKLineSource;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.indicator.Indicator;
import com.helei.tradedatacenter.indicator.calculater.BaseIndicatorCalculator;
import com.helei.tradedatacenter.signal.AbstractSignalMaker;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AutoTradeTask {
    private final StreamExecutionEnvironment env;

    private final List<BaseIndicatorCalculator<? extends Indicator>> indicatorCalList;

    private final List<AbstractSignalMaker> signalMakers;
    private final BaseKLineSource memoryKLineSource;

    public AutoTradeTask(StreamExecutionEnvironment env, MemoryKLineSource memoryKLineSource) {
        // 设置 Flink 流环境
        this.env = env;

        this.memoryKLineSource = memoryKLineSource;

        this.indicatorCalList = new ArrayList<>();
        this.signalMakers = new ArrayList<>();
    }

    public <T extends Indicator> AutoTradeTask addIndicator(BaseIndicatorCalculator<T> calculator) {
        indicatorCalList.add(calculator);
        return this;
    }

    public AutoTradeTask addSignalMaker(AbstractSignalMaker signalMaker) {
        this.signalMakers.add(signalMaker);
        return this;
    }

    public void execute() throws Exception {
        // 使用自定义 SourceFunction 生成 K 线数据流
        KeyedStream<KLine, String> keyedStream = env.addSource(memoryKLineSource)
                .keyBy(KLine::getSymbol);

        SingleOutputStreamOperator<KLine> process = null;
        for (BaseIndicatorCalculator<? extends Indicator> calculator : indicatorCalList) {
            if (process == null) {
                process = keyedStream.process(calculator);
            } else {
                process = process.keyBy(KLine::getSymbol).process(calculator);
            }
        }

        if (signalMakers.size() == 0) {
            throw new IllegalArgumentException("no signal maker");
        }

        KeyedStream<KLine, String> dataStream = null;
        if (process == null) {
            dataStream = keyedStream;
        } else {
            dataStream = process.keyBy(KLine::getSymbol);
        }

        Iterator<AbstractSignalMaker> signalMakerIterator = signalMakers.iterator();
        SingleOutputStreamOperator<TradeSignal> signal = dataStream.process(signalMakerIterator.next());
        while (signalMakerIterator.hasNext()) {
            signal.union(dataStream.process(signalMakerIterator.next()));
        }

        // 4. 输出最终结果
        signal.print();
        env.execute("test1");
    }
}
