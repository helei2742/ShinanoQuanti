package com.helei.dto.config;


import com.alibaba.fastjson.annotation.JSONField;
import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
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

    public static final RunTypeConfig DEFAULT_RUN_TYPE_CONFIG;

    private List<RunEnvTradeTypeConfig> configs;

    private SnowFlowConfig snow_flow;

    static {
        DEFAULT_RUN_TYPE_CONFIG = new RunTypeConfig();
        List<RunEnvTradeTypeConfig> list = new ArrayList<>();
        list.add(new RunEnvTradeTypeConfig(RunEnv.NORMAL, CEXType.BINANCE, List.of(TradeType.SPOT, TradeType.CONTRACT)));
        list.add(new RunEnvTradeTypeConfig(RunEnv.NORMAL, CEXType.BINANCE, List.of(TradeType.CONTRACT, TradeType.CONTRACT)));
        list.add(new RunEnvTradeTypeConfig(RunEnv.TEST_NET, CEXType.BINANCE, List.of(TradeType.SPOT, TradeType.CONTRACT)));
        list.add(new RunEnvTradeTypeConfig(RunEnv.TEST_NET, CEXType.BINANCE, List.of(TradeType.CONTRACT, TradeType.CONTRACT)));

        DEFAULT_RUN_TYPE_CONFIG.setConfigs(list);
        DEFAULT_RUN_TYPE_CONFIG.setSnow_flow(new SnowFlowConfig());
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


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunEnvTradeTypeConfig {
        private RunEnv env;

        private CEXType cexType = CEXType.BINANCE;

        private List<TradeType> trade_type;

        @JSONField(serialize = false)
        public List<KeyValue<RunEnv, TradeType>> getRunTypeList() {
            return trade_type.stream().map(e -> new KeyValue<>(env, e)).toList();
        }
    }
}
