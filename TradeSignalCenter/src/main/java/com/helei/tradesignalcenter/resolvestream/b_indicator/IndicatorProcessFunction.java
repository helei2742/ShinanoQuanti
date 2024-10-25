package com.helei.tradesignalcenter.resolvestream.b_indicator;

import com.helei.dto.KLine;
import com.helei.dto.indicator.Indicator;
import com.helei.dto.indicator.MA;
import com.helei.dto.indicator.config.MAConfig;
import com.helei.tradesignalcenter.resolvestream.b_indicator.calculater.BaseIndicatorCalculator;
import com.helei.util.CalculatorUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
@Setter
public class IndicatorProcessFunction extends KeyedProcessFunction<String, KLine, KLine> {

    private transient VirtualThreadTaskExecutor executor;

    /**
     * 指标计算器
     */
    private final BaseIndicatorCalculator<? extends Indicator>[] indicatorCalList;

    private final int period = 15;

    private transient ValueState<Double> maState;

    public IndicatorProcessFunction(BaseIndicatorCalculator<? extends Indicator>[] indicatorCalList) {
        this.indicatorCalList = indicatorCalList;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        executor = new VirtualThreadTaskExecutor();
        for (BaseIndicatorCalculator<?> calculator : indicatorCalList) {
            calculator.open(parameters, getRuntimeContext());
        }

//        this.maState = getRuntimeContext().getState(new ValueStateDescriptor<>("maState", Double.class));
    }

    @Override
    public void processElement(KLine kLine, KeyedProcessFunction<String, KLine, KLine>.Context context, Collector<KLine> collector) throws Exception {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (BaseIndicatorCalculator<? extends Indicator> calculator : indicatorCalList) {

            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                try {
                    Indicator indicator = calculator.calculateInKLine(kLine);
                    if (indicator != null) {
                        kLine.getIndicators().put(calculator.getIndicatorConfig(), indicator);
                    }
                } catch (Exception e) {
                    log.error("计算指标[{}]发生错误", calculator.getIndicatorConfig().getIndicatorName(), e);
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((v, e) -> {
                    if (e != null) {
                        log.error("计算指标发生错误", e);
                    } else {
                        collector.collect(kLine);
                    }
                });
//        Double ma = this.maState.value();
//        Double close = kLine.getClose();
//
//        if (ma == null) {
//            ma = close;
//        }
//
//        ma = CalculatorUtil.calculateMA(close, ma, period);
//
//        maState.update(ma);
//
//        kLine.getIndicators().put(new MAConfig(15),  new MA(ma));
    }
}
