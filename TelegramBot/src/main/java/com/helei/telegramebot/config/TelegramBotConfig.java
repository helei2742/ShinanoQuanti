package com.helei.telegramebot.config;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.config.RunTypeConfig;
import com.helei.dto.config.TradeSignalConfig;
import com.helei.dto.kafka.KafkaConfig;
import com.helei.dto.kafka.RedisConfig;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class TelegramBotConfig implements Serializable {

    private static final String CONFIG_FILE = "telegram-bot-config.yaml";

    public static final TelegramBotConfig INSTANCE;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = TelegramBotConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> telegram_bot = (Map<String, Object>) quantity.get("telegram_bot");

            INSTANCE = yaml.loadAs(yaml.dump(telegram_bot), TelegramBotConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }

    private RunTypeConfig run_type;

    private List<TelegramBotBaseConfig> bots;

    private KafkaConfig kafka;

    private RedisConfig redis;

    private TradeSignalConfig signal;

    @Data
    public static class TelegramBotBaseConfig {

        /**
         * 运行环境
         */
        private RunEnv runEnv;

        /**
         * 交易类型
         */
        private TradeType tradeType;

        /*
        机器人的用户名
         */
        private String botUsername;

        /**
         * token
         */
        private String token;
    }


    public static void main(String[] args) {
        System.out.println(INSTANCE);
    }
}

