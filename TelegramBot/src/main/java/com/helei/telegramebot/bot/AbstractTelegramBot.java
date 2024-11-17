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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * TG机器人抽象类
 */
@Slf4j
public abstract class AbstractTelegramBot extends TelegramLongPollingBot implements BaseCommandTelegramBot, TradeSignalCommandTelegramBot {


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

                // 处理命令消息
                if (message.isCommand()) {
                    //Step 1 解析命令、参数
                    String[] split = text.split(" ");

                    TelegramBotCommand command = null;
                    String[] commandAndBotName = split[0].split("@");
                    String commandStr = commandAndBotName[0].replace("/", "").toUpperCase();
                    String botName = commandAndBotName[1];

                    //不是本机器人，不管
                    if (!getBotUsername().equals(botName)) {
                        return;
                    }

                    try {
                        command = TelegramBotCommand.valueOf(commandStr);
                    } catch (IllegalArgumentException e) {
                        log.error("不存在命令[{}]", commandStr);
                        sendMessageToChat(String.valueOf(message.getChatId()), "不存在命令 " + commandStr);
                        return;
                    }

                    List<String> params = new ArrayList<>(Arrays.asList(split));
                    params.removeFirst();

                    //Step 2 过滤
                    if (command.equals(TelegramBotCommand.START)) {//1 开始命令

                        startCommandHandler(message);
                    } else if (commandMessageFilter(command, params, message)) {//2 过滤

                        log.warn("bot[{}] 过滤掉 用户[{}] - 消息[{}]", getBotUsername(), from.getUserName(), text);
                    } else {//3 其他命令

                        commandMessageHandler(command, params, message);
                    }
                } else {
                    //处理普通消息
                    normalMessageHandler(text, message);
                }
            } catch (Exception e) {
                log.error("处理消息[{}]出错", message, e);
            }
        });
    }

    /**
     * 过滤命令消息
     *
     * @param command command
     * @param params  params
     * @param message message
     */
    public abstract boolean commandMessageFilter(TelegramBotCommand command, List<String> params, Message message);


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
            log.error("给群组 [{}] 发送消息[{}]发生错误", chatId, messageText, e);
        }
    }

    /**
     * 给指定chat发送html消息
     *
     * @param chatId chatId
     * @param messageText 消息文本
     */
    public void sendHTMLMessageToChat(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode("HTML");
        message.setText(messageText);

        try {
            execute(message);  // 发送消息
        } catch (TelegramApiException e) {
            log.error("给群组 [{}] 发送消息[{}]发生错误", chatId, messageText, e);
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
