package com.helei.dto.config;


import com.alibaba.fastjson.annotation.JSONField;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.base.KeyValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行类型设置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunTypeConfig {

    private List<RunEnvTradeTypeConfig> configs;


    /**
     * 获取运行类型列表
     *
     * @return 类型列表
     */
    @JSONField(serialize = false)
    public List<KeyValue<RunEnv, TradeType>> getRunTypeList() {
        List<KeyValue<RunEnv, TradeType>> list = new ArrayList<>();
        for (RunEnvTradeTypeConfig runEnvTradeTypeConfig : configs) {
            list.addAll(runEnvTradeTypeConfig.getRunTypeList());
        }
        return list;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunEnvTradeTypeConfig {
        private RunEnv env;

        private List<TradeType> trade_type;

        @JSONField(serialize = false)
        public List<KeyValue<RunEnv, TradeType>> getRunTypeList() {
            return trade_type.stream().map(e -> new KeyValue<>(env, e)).toList();
        }
    }
}
