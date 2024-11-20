package com.helei.telegramebot.config;

import com.helei.telegramebot.bot.AbstractTelegramBot;
import com.helei.telegramebot.bot.impl.ShinanoTelegramBot;
import com.helei.telegramebot.manager.ExecutorServiceManager;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import com.helei.telegramebot.service.ITradeSignalPersistenceService;
import com.helei.telegramebot.service.impl.KafkaConsumerService;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class SpringConfig {

    private final TelegramBotConfig telegramBotConfig = TelegramBotConfig.INSTANCE;


    @Autowired
    private ITelegramPersistenceService telegramPersistenceService;

    @Autowired
    private ITradeSignalPersistenceService tradeSignalPersistenceService;

    @Autowired
    @Lazy
    private KafkaConsumerService kafkaConsumerService;

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
//        TelegramBotsApi telegramBotsApi = telegramBotsApi();

        List<AbstractTelegramBot> list = new ArrayList<>();
        for (TelegramBotConfig.TelegramBotBaseConfig botBaseConfig : telegramBotConfig.getBots()) {
            String botUsername = botBaseConfig.getBotUsername();

            try {
//                    log.info("开始注册[{}]tg机器人", botUsername);
                ShinanoTelegramBot bot = new ShinanoTelegramBot(
                        botUsername,
                        botBaseConfig.getToken(),
                        telegramPersistenceService,
                        kafkaConsumerService,
                        Executors.newThreadPerTaskExecutor(new NamedThreadFactory(botUsername + "处理线程池"))
                );

                bot.setTradeSignalPersistenceService(tradeSignalPersistenceService);

                list.add(bot);
//                    telegramBotsApi.registerBot(bot);
            } catch (Exception e) {
                log.error("注册tg机器人[{}]发生错误", botUsername, e);
            }
        }
        return list;
    }

    @Bean
    public Map<String, Object> tgbotKafkaConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, telegramBotConfig.getKafka().getBootstrap_servers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, telegramBotConfig.getKafka().getGroup_id());  // 消费者组ID
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return configProps;
    }

//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(kafkaConfigs());
//    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
        return factory;
    }


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(telegramBotConfig.getRedis().getUrl());
        return Redisson.create(config);
    }
}

