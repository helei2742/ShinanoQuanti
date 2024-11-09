package com.helei.dto.config;


import com.alibaba.fastjson.annotation.JSONField;
import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.KeyValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 运行类型设置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunTypeConfig implements Serializable {

    public static final RunTypeConfig DEFAULT_RUN_TYPE_CONFIG;

    private Set<RunEnvTradeTypeConfig> configs;

    private SnowFlowConfig snow_flow;

    private final HashSet<String> existEnvKeySet = new HashSet<>();

    static {
        DEFAULT_RUN_TYPE_CONFIG = new RunTypeConfig();
        Set<RunEnvTradeTypeConfig> set = new HashSet<>();
        set.add(new RunEnvTradeTypeConfig(RunEnv.NORMAL, CEXType.BINANCE, Set.of(TradeType.SPOT, TradeType.CONTRACT)));
        set.add(new RunEnvTradeTypeConfig(RunEnv.TEST_NET, CEXType.BINANCE, Set.of(TradeType.SPOT, TradeType.CONTRACT)));

        DEFAULT_RUN_TYPE_CONFIG.setConfigs(set);
        DEFAULT_RUN_TYPE_CONFIG.setSnow_flow(new SnowFlowConfig());
    }


    public void setConfigs(Set<RunEnvTradeTypeConfig> configs) {
        this.configs = configs;

        List<KeyValue<RunEnv, TradeType>> runTypeList = getRunTypeList();
        for (KeyValue<RunEnv, TradeType> keyValue : runTypeList) {
            existEnvKeySet.add(keyValue.getKey().name() + "-" + keyValue.getValue().name());
        }
    }

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

    public boolean contains(RunEnv runEnv, TradeType tradeType) {
        return existEnvKeySet.contains(runEnv.name() + "-" + tradeType.name());
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunEnvTradeTypeConfig implements Serializable {
        private RunEnv env;

        private CEXType cexType = CEXType.BINANCE;

        private Set<TradeType> trade_type;

        @JSONField(serialize = false)
        public List<KeyValue<RunEnv, TradeType>> getRunTypeList() {
            return trade_type.stream().map(e -> new KeyValue<>(env, e)).toList();
        }
    }
}
