package com.helei.tradesignalcenter.stream.b_indicator.calculater;

import com.helei.dto.KLine;
import com.helei.dto.indicator.Indicator;
import com.helei.dto.indicator.config.IndicatorConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;


@Getter
@Slf4j
public abstract class BaseIndicatorCalculator<T extends Indicator> implements Serializable {

    protected final IndicatorConfig<T> indicatorConfig;

    protected BaseIndicatorCalculator(IndicatorConfig<T> indicatorConfig) {
        this.indicatorConfig = indicatorConfig;
    }


    public void open(Configuration parameters, RuntimeContext runtimeContext) throws Exception {

    }

    /**
     * 计算指标，放进kLine里
     *
     * @param kLine kLine
     * @return kLine
     */
    public abstract T calculateInKLine(KLine kLine) throws Exception;
}
