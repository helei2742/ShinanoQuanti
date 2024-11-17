package com.helei.telegramebot.config;

import com.helei.dto.base.KeyValue;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class TwitterConfig implements Serializable {
    private static final String CONFIG_FILE = "twitter-config.yaml";

    public static final TwitterConfig INSTANCE;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = TelegramBotConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> twitter = (Map<String, Object>) quantity.get("twitter");

            INSTANCE = yaml.loadAs(yaml.dump(twitter), TwitterConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }


    /**
     * token
     */
    private String bearer_token;

    /**
     * url设置
     */
    private TwitterUrlConfig url;

    /**
     * 规则配置
     */
    private List<KeyValue<String, String>> filter_rule;

    /**
     * twitter的url设置
     */
    @Data
    public static class TwitterUrlConfig implements Serializable {

        /**
         * 注册规则的url
         */
        private String rule_url;

        /**
         * 获取流的url
         */
        private String stream_url;
    }


    public static void main(String[] args) {
        System.out.println(INSTANCE);
    }
}

