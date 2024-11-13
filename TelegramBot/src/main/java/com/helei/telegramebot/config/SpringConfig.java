package com.helei.telegramebot.config;

import com.helei.telegramebot.bot.AbstractTelegramBot;
import com.helei.telegramebot.bot.ShinanoTelegramBot;
import com.helei.telegramebot.manager.ExecutorServiceManager;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import com.helei.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class SpringConfig {

    private final TelegramBotConfig telegramBotConfig = TelegramBotConfig.INSTANCE;


    @Autowired
    private ITelegramPersistenceService telegramPersistenceService;

    @Bean
    public ExecutorServiceManager executorServiceManager() {
        return new ExecutorServiceManager();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            return new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException("创建TelegramBotsApi出错", e);
        }
    }

    @Bean
    @Qualifier("tgBots")
    public List<AbstractTelegramBot> tgBots() {
        TelegramBotsApi telegramBotsApi = telegramBotsApi();

        ExecutorService executor = executorServiceManager().getCommonExecutor();

        for (TelegramBotConfig.TelegramBotBaseConfig botBaseConfig : telegramBotConfig.getBots()) {
            executor.execute(()->{
                String botUsername = botBaseConfig.getBotUsername();

                try {
                    log.info("开始注册[{}]tg机器人", botUsername);
                    ShinanoTelegramBot bot = new ShinanoTelegramBot(
                            botUsername,
                            botBaseConfig.getToken(),
                            telegramPersistenceService,
                            Executors.newThreadPerTaskExecutor(new NamedThreadFactory(botUsername + "处理线程池"))
                    );
                    telegramBotsApi.registerBot(bot);
                } catch (TelegramApiException e) {
                    log.error("注册tg机器人[{}]发生错误", botUsername, e);
                }
            });
        }
        return List.of();
    }

    @Bean
    public Map<String, Object> kafkaConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, telegramBotConfig.getKafka().getBootstrap_servers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, telegramBotConfig.getKafka().getGroup_id());  // 消费者组ID
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return configProps;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(telegramBotConfig.getRedis().getUrl());
        return Redisson.create(config);
    }
}

