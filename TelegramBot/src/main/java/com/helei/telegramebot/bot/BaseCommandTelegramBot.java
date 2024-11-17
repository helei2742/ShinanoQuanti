package com.helei.telegramebot.bot;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BaseCommandTelegramBot {


    /**
     * 处理开始命令
     *
     * @param message message
     */
    void startCommandHandler(Message message);



}
