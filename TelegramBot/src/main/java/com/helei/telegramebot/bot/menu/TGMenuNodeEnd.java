package com.helei.telegramebot.bot.menu;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


public abstract class TGMenuNodeEnd extends TGMenuNode {

    public TGMenuNodeEnd(
            TGMenuNode parentMenu,
            String buttonText,
            String callbackData
    ) {
        super(parentMenu, null, buttonText, callbackData);
    }


    @Override
    public SendMessage getMenu(String chatId) {
        return buildDynamicMenu(chatId);
    }

    public abstract SendMessage buildDynamicMenu(String chatId);
}

