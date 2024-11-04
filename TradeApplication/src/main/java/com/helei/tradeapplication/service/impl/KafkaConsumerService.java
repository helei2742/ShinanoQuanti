package com.helei.tradeapplication.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.base.KeyValue;
import com.helei.tradeapplication.config.TradeAppConfig;
import com.helei.tradeapplication.listener.KafkaTradeSignalListener;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class KafkaConsumerService implements InitializingBean {

    private final TradeAppConfig tradeAppConfig = TradeAppConfig.INSTANCE;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @Autowired
    private ExecutorServiceManager executorServiceManager;

    @Autowired
    private KafkaTradeSignalService tradeSignalService;

    /**
     * 开始交易信号消费
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public void startTradeSignalConsumer(RunEnv env, TradeType tradeType) {

        tradeAppConfig.getSignalTopics(env, tradeType, (prefix, signalNames) -> {
            if (signalNames.isEmpty()) {
                log.warn("没有配置env[{}]-tradeType[{}]类型的交易信号topic", env, tradeType);
                return;
            }
            log.info("注册监听topic [{}*] 交易信号 ", prefix);
            List<String> topics = signalNames.stream().map(name -> (prefix + name).toLowerCase()).toList();
            startConsumer(topics, new KafkaTradeSignalListener(env, tradeType, tradeSignalService, executorServiceManager.getTradeSignalResolveExecutor()));
        });
    }


    /**
     * 开始kafka消费
     *
     * @param topics          topics
     * @param messageListener messageListener
     */
    public void startConsumer(List<String> topics, MessageListener<String, String> messageListener) {
        ConcurrentMessageListenerContainer<String, String> container = kafkaListenerContainerFactory.createContainer(topics.toArray(new String[0]));

        container.setupMessageListener(messageListener);
        container.start();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (KeyValue<RunEnv, TradeType> keyValue : tradeAppConfig.getRun_type().getRunTypeList()) {
            startTradeSignalConsumer(keyValue.getKey(), keyValue.getValue());
        }
    }
}


