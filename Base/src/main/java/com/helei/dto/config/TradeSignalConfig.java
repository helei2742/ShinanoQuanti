package com.helei.dto.config;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeSignalConfig {
    /**
     * 主网环境信号配置
     */
    private TradeSignalEnvConfig normal;

    /**
     * 测试环境信号配置
     */
    private TradeSignalEnvConfig test_net;


    /**
     * 根据环境获取配置
     *
     * @param runEnv    运行环境
     * @param tradeType 交易类型
     * @return 交易类型列表
     */
    public List<TradeSignalSymbolConfig> getEnvSignalSymbolConfig(RunEnv runEnv, TradeType tradeType) {
        return switch (runEnv) {
            case TEST_NET -> test_net.getTradeSignalSymbolConfigs(tradeType);
            case NORMAL -> normal.getTradeSignalSymbolConfigs(tradeType);
        };
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeSignalEnvConfig {

        /**
         * 现货类型信号配置
         */
        private List<TradeSignalSymbolConfig> spot;

        /**
         * u本位合约类型信号设置
         */
        private List<TradeSignalSymbolConfig> contract;

        public List<TradeSignalSymbolConfig> getTradeSignalSymbolConfigs(TradeType tradeType) {
            return switch (tradeType) {
                case SPOT -> spot;
                case CONTRACT -> contract;
            };
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeSignalSymbolConfig {

        /**
         * 交易对名称
         */
        private String symbol;

        /**
         * 信号名list
         */
        private List<String> signal_names;
    }
}
