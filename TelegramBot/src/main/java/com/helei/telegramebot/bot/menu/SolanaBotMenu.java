package com.helei.telegramebot.bot.menu;

import com.helei.dto.base.Result;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.Serializable;

public class SolanaBotMenu implements Serializable, TelegramBotMenu {

    private final String botUsername;

    private final ITelegramPersistenceService telegramPersistenceService;

    public SolanaBotMenu(String botUsername, ITelegramPersistenceService telegramPersistenceService) {
        this.botUsername = botUsername;
        this.telegramPersistenceService = telegramPersistenceService;
    }


    @Override
    public SendMessage initChatMenu(String chatId) {
        Result result = telegramPersistenceService.saveChatMenuState(botUsername, chatId, SolanaBotMenuType.MAIN);

        if (result.getSuccess()) {
            SolanaBotMenuType menuType = SolanaBotMenuType.valueOf((String) result.getData());
            return menuType.getMenu(chatId);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(result.getErrorMsg());
            return sendMessage;
        }
    }

    @Override
    public TelegramBotMenuType getCurrentMenuState(String chatId) {
        Result result = telegramPersistenceService.getChatMenuState(botUsername, chatId);

        if (result.getSuccess()) {
            return SolanaBotMenuType.valueOf((String) result.getData());
        } else {
            return SolanaBotMenuType.MAIN;
        }
    }

    @Override
    public TelegramBotMenuType getPrefer(String chatId) {

        return getCurrentMenuState(chatId).getPrefer();
    }


    @Override
    public SendMessage getCurrentMenu(String chatId) {
        SolanaBotMenuType currentMenuState = (SolanaBotMenuType) getCurrentMenuState(chatId);

        SendMessage menu = currentMenuState.getMenu(chatId);
        menu.setChatId(chatId);
        return menu;
    }

    @Override
    public SendMessage menuCommandHandler(String menuCommand, Message message) {
        SolanaBotMenuType solanaBotMenuType = SolanaBotMenuType.valueOf(menuCommand);

        return solanaBotMenuType.getMenu(String.valueOf(message.getChatId()));
    }

}

