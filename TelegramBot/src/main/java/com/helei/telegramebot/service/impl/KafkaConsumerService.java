package com.helei.telegramebot.service.impl;


import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.KeyValue;
import com.helei.dto.config.TradeSignalConfig;
import com.helei.telegramebot.bot.AbstractTelegramBot;
import com.helei.telegramebot.config.TelegramBotConfig;
import com.helei.telegramebot.manager.ExecutorServiceManager;
import com.helei.telegramebot.listener.KafkaTradeSignalTGBotListener;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KafkaConsumerService {

    private final TelegramBotConfig telegramBotConfig = TelegramBotConfig.INSTANCE;

    @Autowired
    @Lazy
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @Autowired
    @Qualifier("tgbotKafkaConfigs")
    private Map<String, Object> tgbotKafkaConfigs;

    @Autowired
    private ExecutorServiceManager executorServiceManager;

    @Autowired
    private List<AbstractTelegramBot> tgBots;


    /**
     * 开始交易信号消费
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public void startTradeSignalConsumer(RunEnv env, TradeType tradeType) {
        List<TradeSignalConfig.TradeSignalSymbolConfig> envSignalSymbolConfig = telegramBotConfig.getSignal().getEnvSignalSymbolConfig(env, tradeType);
        if (envSignalSymbolConfig == null) return;

        List<String> topics = new ArrayList<>();
        for (TradeSignalConfig.TradeSignalSymbolConfig tradeSignalSymbolConfig : envSignalSymbolConfig) {
            String symbol = tradeSignalSymbolConfig.getSymbol();
            for (String signalName : tradeSignalSymbolConfig.getSignal_names()) {
                topics.add(KafkaUtil.getTradeSingalTopic(env, tradeType, symbol, signalName));
            }
        }

        log.info("开始监听kafka交易信号, [{}]", topics);


        startTelegramBotTradeSignalConsumer(topics);
    }

    /**
     * 开启tg机器人交易信号消费
     *
     * @param topics topics
     */
    public void startTelegramBotTradeSignalConsumer(List<String> topics) {
        for (AbstractTelegramBot tgBot : tgBots) {
            try {
                startConsumer(topics, "telegram-bot" + tgBot.getBotUsername(), new KafkaTradeSignalTGBotListener(tgBot, executorServiceManager));
                log.info("已为[{}]telegram bot注册监听交易信号", tgBot.getBotUsername());
            } catch (Exception e) {
                log.error("telegram bot[{}]监听交易信号发生错误", tgBot.getBotUsername(), e);
            }
        }
    }


    /**
     * 开始kafka消费
     *
     * @param topics          topics
     * @param groupId         groupId
     * @param messageListener messageListener
     */
    public void startConsumer(List<String> topics, String groupId, MessageListener<String, String> messageListener) {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> o = (Map<String, Object>) tgbotKafkaConfigs.get("tgbotKafkaConfigs");
        for (String s : o.keySet()) {
            config.put(s, o.get(s));
        }
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        kafkaListenerContainerFactory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config));
        ConcurrentMessageListenerContainer<String, String> container = kafkaListenerContainerFactory.createContainer(topics.toArray(new String[0]));
        container.setupMessageListener(messageListener);
        container.start();
    }

    /**
     * 开启所有环境的信号消费
     */
    public void startAllTradeSignalConsumer() {
        for (KeyValue<RunEnv, TradeType> keyValue : telegramBotConfig.getRun_type().getRunTypeList()) {
            startTradeSignalConsumer(keyValue.getKey(), keyValue.getValue());
        }
    }
}
