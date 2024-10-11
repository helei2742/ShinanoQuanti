

package com.helei.tradedatacenter.indicator.calculater;

import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.Indicator;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;


@Slf4j
public abstract class BaseIndicatorCalculator<T extends Indicator> extends KeyedProcessFunction<String, KLine, KLine> {

    @Override
    public void processElement(KLine kLine, KeyedProcessFunction<String, KLine, KLine>.Context context, Collector<KLine> collector){
        try {
            T Indicator = calculateInKLine(kLine);
            kLine.getIndicators().put(indicatorKey(Indicator), Indicator);
            collector.collect(kLine);
        } catch (Exception e) {
            log.error("calculate indicator error", e);
            throw new RuntimeException(e);
        }
    }

    public abstract String indicatorKey(T indicator) throws Exception;

    /**
     * 计算指标，放进kLine里
     * @param kLine kLine
     * @return kLine
     */
    public abstract T calculateInKLine(KLine kLine) throws Exception;
}
