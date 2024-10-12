
package com.helei.tradedatacenter;

import com.helei.tradedatacenter.datasource.BaseKLineSource;
import com.helei.tradedatacenter.datasource.MemoryKLineSource;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.Indicator;
import com.helei.tradedatacenter.indicator.calculater.BaseIndicatorCalculator;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;


public class AutoTradeTask {
    private final StreamExecutionEnvironment env;

    private final List<BaseIndicatorCalculator<? extends Indicator>> indicatorCalList;

    private final BaseKLineSource memoryKLineSource;

    public AutoTradeTask(StreamExecutionEnvironment env, MemoryKLineSource memoryKLineSource) {
        // 设置 Flink 流环境
        this.env = env;

        this.memoryKLineSource = memoryKLineSource;

        this.indicatorCalList = new ArrayList<>();
    }

    public <T extends Indicator> AutoTradeTask addIndicator(BaseIndicatorCalculator<T> calculator) {
        indicatorCalList.add(calculator);
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

        SingleOutputStreamOperator<String> finalResultStream = null;
        if (process == null) {
            finalResultStream = keyedStream.process(new KeyedProcessFunction<String, KLine, String>() {
                @Override
                public void processElement(KLine kLine, Context context, Collector<String> collector) throws Exception {
                    collector.collect(kLine.toString());
                }
            });
        } else {
            finalResultStream = process.keyBy(KLine::getSymbol).process(new KeyedProcessFunction<String, KLine, String>() {
                @Override
                public void processElement(KLine kLine, Context context, Collector<String> collector) throws Exception {
                    collector.collect(kLine.toString());
                }
            });
        }

        // 4. 输出最终结果
        finalResultStream.print();
        env.execute("test1");
    }
}
