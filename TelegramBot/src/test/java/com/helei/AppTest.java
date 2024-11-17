package com.helei;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.telegramebot.bot.AbstractTelegramBot;
import com.helei.telegramebot.bot.impl.ShinanoTelegramBot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class AppTest {

    @Autowired
    @Qualifier("tgBots")
    public List<AbstractTelegramBot> tgBots;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testAddListenSignalChat() throws InterruptedException {
        ShinanoTelegramBot bot = (ShinanoTelegramBot) tgBots.getFirst();
        List<Object> parmas = new ArrayList<>();
        parmas.add(RunEnv.TEST_NET.name());
        parmas.add(TradeType.CONTRACT.name());
        parmas.add(CEXType.BINANCE.name());
        parmas.add("btcusdt");
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(11111111L);
        message.setChat(chat);
        bot.addListenSignalTypeCommandHandler(parmas, message);


        TimeUnit.MINUTES.sleep(199);
    }

    @AfterEach
    void tearDown() {
    }
}
