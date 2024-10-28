package com.helei.dto.indicator.config;

import com.alibaba.fastjson.JSONObject;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 指标配置
 */
@EqualsAndHashCode(callSuper = false)
public abstract class IndicatorConfig<T> implements Serializable{
    @Serial
    private static final long serialVersionUID = 111111111L; // 显式声明 serialVersionUID

    public final Class<T> indicatorClass;

    public final String name;


    protected IndicatorConfig(Class<T> indicatorClass) {
        this.indicatorClass = indicatorClass;
        this.name = indicatorClass.getSimpleName();
    }

    public abstract String getIndicatorName();
}




