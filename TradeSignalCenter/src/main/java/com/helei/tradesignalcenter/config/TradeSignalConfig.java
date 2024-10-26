package com.helei.tradesignalcenter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Data
public class TradeSignalConfig {
    private static final String CONFIG_FILE = "trade-signal-config.yaml";

    public static final TradeSignalConfig TRADE_SIGNAL_CONFIG;

    private RealtimeKafkaConfig kafka;

    private RealtimeFlinkConfig flink;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = TradeSignalConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> realtimeData = (Map<String, Object>) quantity.get("realtime");


            TRADE_SIGNAL_CONFIG = yaml.loadAs(yaml.dump(realtimeData), TradeSignalConfig.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }

    private TradeSignalConfig() {}

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RealtimeKafkaConfig {
        /**
         * kafka集群连接地址
         */
        private String bootstrapServer;

        /**
         * 消费者组名
         */
        private String groupId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RealtimeFlinkConfig {

        /**
         * flink job manager host
         */
        private String jobManagerHost;
        /**
         * flink job manager port
         */
        private Integer jobManagerPort;
    }

    public static void main(String[] args) {
        System.out.println(TRADE_SIGNAL_CONFIG);
    }
}
