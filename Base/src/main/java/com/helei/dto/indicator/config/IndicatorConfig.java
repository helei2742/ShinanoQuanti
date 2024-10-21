
package com.helei.dto.indicator.config;

        import com.alibaba.fastjson.JSONObject;
        import lombok.EqualsAndHashCode;

        import java.io.Serializable;

/**
 * 指标配置
 */
@EqualsAndHashCode(callSuper = false)
public abstract class IndicatorConfig<T> implements Serializable {
    public final Class<T> indicatorClass;

    public final String name;

    public final JSONObject config = new JSONObject();;

    protected IndicatorConfig(Class<T> indicatorClass) {
        this.indicatorClass = indicatorClass;
        this.name = indicatorClass.getSimpleName();
    }

    public abstract String getIndicatorName();
}




