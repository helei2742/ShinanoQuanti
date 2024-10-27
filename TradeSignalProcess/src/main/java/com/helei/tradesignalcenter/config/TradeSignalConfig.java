package com.helei.tradesignalcenter.config;

import com.helei.constants.TradeType;
import com.helei.tradesignalcenter.constants.RunEnv;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

@Data
public class TradeSignalConfig implements Serializable {
    private static final String CONFIG_FILE = "trade-signal-config.yaml";

    public static final TradeSignalConfig TRADE_SIGNAL_CONFIG;

    /**
     * 运行环境，测试网或者普通网
     */
    private RunEnv run_env;

    /**
     * 交易类型
     */
    private TradeType trade_type;

    /**
     * 历史k线加载批大小
     */
    private int historyKLineBatchSize;

    /**
     * 批加载并发度
     */
    private int batchLoadConcurrent;

    /**
     * 实时数据配置
     */
    private RealtimeConfig realtime;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = TradeSignalConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> trade_signal_maker = (Map<String, Object>) quantity.get("trade_signal_maker");


            TRADE_SIGNAL_CONFIG = yaml.loadAs(yaml.dump(trade_signal_maker), TradeSignalConfig.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }

    private TradeSignalConfig() {}



    @Data
    public static class RealtimeConfig  implements Serializable  {

        private RealtimeKafkaConfig kafka;

        private RealtimeFlinkConfig flink;

    }


    @Data
    public static class RealtimeKafkaConfig  implements Serializable  {
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
    public static class RealtimeFlinkConfig  implements Serializable  {

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
