package com.helei.tradeapplication.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.tradeapplication.config.TradeAppConfig;
import com.helei.tradeapplication.listener.KafkaTradeOrderListener;
import com.helei.tradeapplication.listener.KafkaTradeSignalListener;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.TradeOrderCommitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class KafkaConsumerService {

    private final TradeAppConfig tradeAppConfig = TradeAppConfig.INSTANCE;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @Autowired
    private ExecutorServiceManager executorServiceManager;

    @Autowired
    private KafkaTradeSignalService tradeSignalService;

    @Autowired
    private TradeOrderCommitService tradeOrderCommitService;

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
            log.info("注册监听topic [{}*] signalNames[{}]交易信号 ", prefix, signalNames);
            List<String> topics = signalNames.stream().map(name -> (prefix + name).toLowerCase()).toList();
            startConsumer(topics, new KafkaTradeSignalListener(env, tradeType, tradeSignalService, executorServiceManager.getTradeSignalResolveExecutor()));
        });
    }


    /**
     * 开始消费交易订单
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public void startTradeOrderConsumer(RunEnv env, TradeType tradeType) {
        tradeAppConfig.getTradeOrderTopics(env, tradeType, topics->{
            startConsumer(topics, new KafkaTradeOrderListener(env, tradeType, tradeOrderCommitService, executorServiceManager.getTradeOrderResolveExecutor()));
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
}
