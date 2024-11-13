package com.helei.tradeapplication.config;

import com.alibaba.fastjson.JSON;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.config.RunTypeConfig;
import com.helei.dto.config.TradeSignalConfig;
import com.helei.dto.kafka.KafkaConfig;
import com.helei.dto.kafka.RedisConfig;
import com.helei.util.KafkaUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeAppConfig {
    private static final String CONFIG_FILE = "trade-app-config.yaml";

    public static final TradeAppConfig INSTANCE;

    private RunTypeConfig run_type;

    private RedisConfig redis;

    private KafkaConfig kafka;

    private TradeSignalConfig signal;


    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = BinanceApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> trade_app = (Map<String, Object>) quantity.get("trade_app");

            INSTANCE = yaml.loadAs(yaml.dump(trade_app), TradeAppConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }


    /**
     * 获取信号topics，通过回调的方式，不直接返回topic列表
     *
     * @param env          运行环境
     * @param tradeType    交易类型
     * @param topicResolve 回调函数， 第一个参数为前缀， 第二个参数为信号名列表
     */
    public void getSignalTopics(RunEnv env, TradeType tradeType, BiConsumer<String, List<String>> topicResolve) {
        StringBuilder prefix = new StringBuilder(env.name());
        prefix.append(".").append(tradeType.name()).append(".");

        List<TradeSignalConfig.TradeSignalSymbolConfig> scList = signal.getEnvSignalSymbolConfig(env, tradeType);

        if (scList == null) {
            topicResolve.accept(prefix.toString(), Collections.emptyList());
            return;
        }

        for (TradeSignalConfig.TradeSignalSymbolConfig signalSymbolConfig : scList) {
            String symbol = signalSymbolConfig.getSymbol();
            topicResolve.accept(prefix + symbol + ".", signalSymbolConfig.getSignal_names());
        }
    }


    /**
     * 获取交易订单的topic
     *
     * @param env          运行环境
     * @param tradeType    交易信号
     * @param topicResolve 回调
     */
    public void getTradeOrderTopics(RunEnv env, TradeType tradeType, Consumer<List<String>> topicResolve) {

        List<TradeSignalConfig.TradeSignalSymbolConfig> scList = switch (env) {
            case TEST_NET -> signal.getTest_net().getTradeSignalSymbolConfigs(tradeType);
            case NORMAL -> signal.getNormal().getTradeSignalSymbolConfigs(tradeType);
        };

        if (scList == null) {
            topicResolve.accept(Collections.emptyList());
            return;
        }

        topicResolve.accept(scList.stream().map(e -> KafkaUtil.getOrderSymbolTopic(env, tradeType, e.getSymbol())).toList());
    }



    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(INSTANCE));
    }
}
