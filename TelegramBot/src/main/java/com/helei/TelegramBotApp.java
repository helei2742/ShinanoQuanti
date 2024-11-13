package com.helei;

import com.helei.telegramebot.service.impl.KafkaConsumerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class TelegramBotApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TelegramBotApp.class, args);


        startAllEnvTradeSignalConsumer(applicationContext);
    }


    /**
     * 开启所有信号消费
     *
     * @param applicationContext app
     */
    private static void startAllEnvTradeSignalConsumer(ApplicationContext applicationContext) {
        KafkaConsumerService kafkaConsumerService = applicationContext.getBean(KafkaConsumerService.class);
        kafkaConsumerService.startAllTradeSignalConsumer();
    }
}
