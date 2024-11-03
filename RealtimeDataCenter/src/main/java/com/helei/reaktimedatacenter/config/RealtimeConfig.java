package com.helei.reaktimedatacenter.config;


import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.constants.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.reaktimedatacenter.dto.SymbolKLineInfo;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class RealtimeConfig {
    private static final String CONFIG_FILE = "realtime-data-config.yaml";

    public static final RealtimeConfig INSTANCE;

    private RealtimeBaseConfig base;

    private RealtimeEnvConfig normal;

    private RealtimeEnvConfig test_net;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = BinanceApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> realtime = (Map<String, Object>) quantity.get("realtime");

            INSTANCE = yaml.loadAs(yaml.dump(realtime), RealtimeConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }

    /**
     * 根据env 和 tradeType。选取合适的实时k线数据设置
     * @param runEnv runEnv
     * @param tradeType tradeType
     * @return RealtimeKLineDataConfig 誓死k线数据设置
     */
    public RealtimeKLineDataConfig getEnvKLineDataConfig(RunEnv runEnv, TradeType tradeType) {
        return switch (runEnv) {
            case NORMAL -> normal.getRTKLineDataConfigByTradeType(tradeType);
            case TEST_NET -> test_net.getRTKLineDataConfigByTradeType(tradeType);
        };
    }


    @Data
    public static class RealtimeBaseConfig {

        /**
         * kafka写入实时k线时设置几个分区
         */
        private int kafka_kline_num_partitions;

        /**
         * kafka的副本个数
         */
        private short kafka_kline_replication_factor;
    }

    @Data
    public static class RealtimeEnvConfig {


        /**
         * 现货设置
         */
        private RealtimeKLineDataConfig spot;

        /**
         * 合约设置
         */
        private RealtimeKLineDataConfig contract;


        public RealtimeKLineDataConfig getRTKLineDataConfigByTradeType(TradeType tradeType) {
            return switch (tradeType) {
                case SPOT -> spot;
                case CONTRACT -> contract;
            };
        }
    }

    @Data
    public static class RealtimeKLineDataConfig {

        private List<String> listen_kline;

        /**
         * 客户端监听k线最大的数量
         */
        private int client_listen_kline_max_count = 20;

        /**
         * 实时的k线种类
         */
        private List<SymbolKLineInfo> realtimeKLineList;


        public void setListen_kline(List<String> listen_kline) {
            realtimeKLineList = listen_kline.stream().map(s -> {
                String[] split = s.split("@");
                String symbol = split[0];
                Set<KLineInterval> set = Arrays.stream(split[1].split(","))
                        .map(KLineInterval.STATUS_MAP::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                return new SymbolKLineInfo(symbol, set);
            }).collect(Collectors.toList());

            this.listen_kline = listen_kline;
        }
    }

    public static void main(String[] args) {
        System.out.println(
                INSTANCE
        );
    }
}
