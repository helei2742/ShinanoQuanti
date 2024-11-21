package com.helei.telegramebot.bot;

import com.helei.dto.base.Result;
import com.helei.telegramebot.bot.menu.TelegramBotMenu;
import com.helei.telegramebot.config.command.TelegramBotNameSpaceCommand;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.concurrent.ExecutorService;


@Slf4j
public abstract class MenuBaseTelegramBot extends AbstractTelegramBot {

    /**
     * 菜单
     */
    protected TelegramBotMenu telegramBotMenu;


    protected MenuBaseTelegramBot(String botUsername, String token, ITelegramPersistenceService telegramPersistenceService, ExecutorService executor) {
        super(botUsername, token, telegramPersistenceService, executor);
    }


    public void init(TelegramBotMenu telegramBotMenu) {
        this.telegramBotMenu = telegramBotMenu;
    }


    @Override
    public boolean commandMessageFilter(TelegramBotNameSpaceCommand.NameSpace nameSpace, String nameSpaceCommand, List<String> params, Message message) {
        Result result = getTelegramPersistenceService().isSavedChatInBot(getBotUsername(), message.getChatId());

        // 过滤掉没初始化的chat
        if (!result.getSuccess()) {
            sendMessageToChat(String.valueOf(message.getChatId()), result.getErrorMsg());
            return true;
        }

        return false;
    }

    @Override
    public Result menuCommandHandler(String menuCommand, Message message) {
        BotApiMethod<?> botApiMethod = telegramBotMenu.menuCommandHandler(menuCommand, message);
        try {
            if (botApiMethod != null) {
                execute(botApiMethod);
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(String.format("[%s]处理菜单命令[%s]失败, %s", message.getChatId(), menuCommand, e.getMessage()));
        }
    }


    @Override
    public void startCommandHandler(Message message) {
        Long chatId = message.getChatId();

        // chatId持久化，连同用户信息
        User from = message.getFrom();

        Result result = getTelegramPersistenceService().saveChatInBot(getBotUsername(), chatId, from);
        if (!result.getSuccess()) {
            log.error("保存聊天[{}]用户[{}]信息失败", chatId, from.getUserName());
            sendMessageToChat(String.valueOf(chatId), result.getErrorMsg());
        } else {
            sendMessageToChat(String.valueOf(chatId), getBotUsername() + " 注册聊天信息成功");
        }


        try {
            SendMessage sendMessage = telegramBotMenu.initChatMenu(String.valueOf(chatId));
            execute(sendMessage);
        } catch (Exception e) {
            log.error("[{}]创建菜单失败", chatId, e);
            sendMessageToChat(String.valueOf(chatId), "创建菜单失败");
        }
    }
}
