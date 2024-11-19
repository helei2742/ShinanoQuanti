package com.helei;

import com.helei.telegramebot.bot.ShinanoTelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(App.class, args);

        TelegramBotsApi telegramBotsApi = applicationContext.getBean(TelegramBotsApi.class);
        try {
            telegramBotsApi.registerBot(new ShinanoTelegramBot("heleidage666_bot", "7680003612:AAGBiX-2-DtItBTd_LRxLrCj7-lp04fNtxg"));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
