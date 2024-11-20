package com.helei.telegramebot.bot.menu;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramBotMenu {


    SendMessage initChatMenu(String chatId);

    TGMenuNode getCurrentMenuState(String chatId);

    /**
     * 获取 id 当前菜单上一级的菜单
     *
     * @return 上一级菜单
     */
    TGMenuNode getPrefer(String chatId);


    /**
     * 获取当前菜单
     *
     * @param chatId chatId
     * @return 菜单
     */
    SendMessage getCurrentMenu(String chatId);


    /**
     * 处理菜单命令
     *
     * @param menuCommand menuCommand
     * @param message     message
     * @return SendMessage
     */
    SendMessage menuCommandHandler(String menuCommand, Message message);
}


