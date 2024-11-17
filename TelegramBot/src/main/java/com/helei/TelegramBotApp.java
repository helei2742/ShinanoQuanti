package com.helei;

import com.helei.telegramebot.bot.AbstractTelegramBot;
import com.helei.telegramebot.service.impl.KafkaConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.util.List;

@Slf4j
@SpringBootApplication
public class TelegramBotApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TelegramBotApp.class, args);


        startAllEnvTradeSignalConsumer(applicationContext);

        startAllTGBot(applicationContext);
    }


    /**
     * 开启所有信号消费
     *
     * @param applicationContext app
     */
    private static void startAllEnvTradeSignalConsumer(ApplicationContext applicationContext) {
        log.info("开始消费交易信号.....");
        KafkaConsumerService kafkaConsumerService = applicationContext.getBean(KafkaConsumerService.class);
        kafkaConsumerService.startAllTradeSignalConsumer();
        log.info("交易信号消费启动完成.....");
    }

    /**
     * 开启所有的tg机器人
     *
     * @param applicationContext app
     */
    private static void startAllTGBot(ApplicationContext applicationContext) {
        try {
            log.info("开始启动tg机器人.....");
            TelegramBotsApi telegramBotsApi = applicationContext.getBean(TelegramBotsApi.class);

            List<AbstractTelegramBot> tgBots = (List<AbstractTelegramBot>) applicationContext.getBean("tgBots");

            for (AbstractTelegramBot tgBot : tgBots) {
                log.info("启动[{}}机器人", tgBot.getBotUsername());
                telegramBotsApi.registerBot(tgBot);
            }

            log.info("tg机器人启动完成");
        } catch (Exception e) {
            log.error("开启Telegram Bot发生异常", e);
        }
    }
}
