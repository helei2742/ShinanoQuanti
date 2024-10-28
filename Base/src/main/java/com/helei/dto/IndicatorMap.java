package com.helei.dto;

import com.helei.dto.indicator.Indicator;
import com.helei.dto.indicator.config.IndicatorConfig;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;

@ToString
public class IndicatorMap {

    @Getter
    private final HashMap<String, Indicator> map;


    public IndicatorMap() {
        this.map = new HashMap<>();
    }


    public <T extends Indicator> T getIndicator(IndicatorConfig<T> config) {
        return (T) getIndicator(config.getIndicatorName());
    }
    public Indicator getIndicator(String indicatorName) {
        return map.get(indicatorName);
    }

    public void put(IndicatorConfig<? extends Indicator> indicatorConfig, Indicator indicator) {
        map.put(indicatorConfig.getIndicatorName(), indicator);
    }


}
