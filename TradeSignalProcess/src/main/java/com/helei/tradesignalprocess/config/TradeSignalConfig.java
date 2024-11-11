package com.helei.tradesignalprocess.config;

import com.helei.constants.trade.TradeType;
import com.helei.constants.RunEnv;
import com.helei.dto.config.RunTypeConfig;
import com.helei.dto.kafka.TradeSignalTopic;
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
     * 信号名
     */
    private String name;

    /**
     * 信号交易对
     */
    private String symbol;

    /**
     * 运行环境设置
     */
    private RunTypeConfig run_type;

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

    private TradeSignalConfig() {
    }

    /**
     * 获取最终交易信号写入到kafka到topic
     *
     * @return topic
     */
    public TradeSignalTopic getSinkTopic() {
        return new TradeSignalTopic(getRun_env(), getTrade_type(), symbol, name);
    }

    /**
     * 获取交易类型
     *
     * @return TradeType
     */
    public TradeType getTrade_type() {
        return run_type.getConfigs().stream().findFirst().get().getTrade_type().stream().findFirst().get();
    }

    /**
     * 获取运行环境
     *
     * @return RunEnv
     */
    public RunEnv getRun_env() {
        return run_type.getConfigs().stream().findFirst().get().getEnv();
    }


    @Data
    public static class RealtimeConfig implements Serializable {

        private RealtimeKafkaConfig kafka;

        private RealtimeFlinkConfig flink;

    }


    @Data
    public static class RealtimeKafkaConfig implements Serializable {

        /**
         * 输入的配置
         */
        private KafkaServerConfig input;

        /**
         * 输出的配置
         */
        private KafkaServerConfig output;

    }

    @Data
    public static class KafkaServerConfig implements Serializable {
        /**
         * kafka集群连接地址
         */
        private String bootstrapServer;

        /**
         * 消费者组名
         */
        private String groupId;

        /**
         * 事务超时时间，需要比kafka broker 中设置的小
         */
        private String transaction_timeout_ms;
    }

    @Data
    public static class RealtimeFlinkConfig implements Serializable {

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
