package com.helei.dto;

import com.helei.dto.indicator.Indicator;
import com.helei.dto.indicator.config.IndicatorConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class IndicatorMap {

    private final ConcurrentHashMap<String, Indicator> map;


    public IndicatorMap() {
        this.map = new ConcurrentHashMap<>();
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

    @Override
    public String toString() {
        return "IndicatorMap{" +
                "map=" + map +
                '}';
    }
}
