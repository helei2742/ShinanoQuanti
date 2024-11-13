package com.helei.telegramebot.bot;

import com.helei.telegramebot.constants.TelegramBotCommand;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * TG机器人抽象类
 */
@Slf4j
public abstract class AbstractTelegramBot extends TelegramLongPollingBot {


    /**
     * 机器人名
     */
    private final String botUsername;

    /**
     * 机器人连接的token
     */
    private final String token;

    /**
     * 持久化服务
     */
    @Getter
    private final ITelegramPersistenceService telegramPersistenceService;

    /**
     * 执行的线程池
     */
    protected final ExecutorService executor;

    protected AbstractTelegramBot(String botUsername, String token, ITelegramPersistenceService telegramPersistenceService, ExecutorService executor) {
        this.botUsername = botUsername;
        this.token = token;
        this.telegramPersistenceService = telegramPersistenceService;
        this.executor = executor;
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        executor.execute(() -> {
            Message message = update.getMessage();
            try {
                User from = message.getFrom();
                String text = message.getText();

                log.info("bot[{}] 收到消息 用户[{}] - 消息[{}]", getBotUsername(), from.getUserName(), text);

                if (message.isCommand()) {
                    //处理命令消息
                    String[] split = text.split(" ");
                    TelegramBotCommand command = TelegramBotCommand.valueOf(split[0]);
                    List<String> params = Arrays.stream(split).toList();
                    params.removeFirst();
                    commandMessageHandler(command, params, message);
                } else {
                    //处理普通消息
                    normalMessageHandler(text, message);
                }
            } catch (Exception e) {
                log.error("处理消息[{}]出错", message);
            }
        });
    }


    /**
     * 命令消息处理
     *
     * @param command 命令
     * @param params  参数
     * @param message 原消息内容
     */
    public abstract void commandMessageHandler(TelegramBotCommand command, List<?> params, Message message);

    /**
     * 普通消息处理
     *
     * @param messageText 消息文本
     * @param message     原消息内容
     */
    public abstract void normalMessageHandler(String messageText, Message message);


    /**
     * 给指定chat发消息
     *
     * @param chatId      chatId
     * @param messageText 消息文本
     */
    public void sendMessageToChat(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);  // 发送消息
        } catch (TelegramApiException e) {
            log.error("给群组 [{}] 发送消息发生错误", chatId, e);
        }
    }


    /**
     * 获取聊天信息
     *
     * @param chatId chatId
     * @return 详细信息
     */
    public Chat getChat(String chatId) {
        GetChat chat = new GetChat(chatId);
        try {
            return execute(chat);
        } catch (TelegramApiException e) {
            log.error("获取Chat[{}]消息发生错误", chatId);
        }
        return null;
    }


    /**
     * 获取特定成员信息
     *
     * @param chatId 群组id
     * @param userId 消息文本
     */
    public User getChatMemberInfo(String chatId, long userId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId);
        getChatMember.setUserId(userId);

        try {
            ChatMember chatMember = execute(getChatMember);
            log.info("获取到群组[{}]成员[{}]的信息, status[{}]", chatId, userId, chatMember.getStatus());
            // 根据需要获取更多信息，例如用户名、权限等
            return chatMember.getUser();
        } catch (TelegramApiException e) {
            throw new RuntimeException(String.format("获取到群组[%s]成员[%s]的信息失败", chatId, userId), e);
        }
    }

    /**
     * 获取群组管理员信息
     *
     * @param chatId 群组id
     */
    public List<ChatMember> getChatAdministratorsInfo(String chatId) {
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);

        try {
            return execute(getChatAdministrators);
        } catch (TelegramApiException e) {
            throw new RuntimeException(String.format("获取到群组[%s]管理员信息出错", chatId), e);
        }
    }

}
