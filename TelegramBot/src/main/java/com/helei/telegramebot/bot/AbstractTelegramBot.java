package com.helei.telegramebot.bot;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.helei.dto.base.Result;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import com.helei.telegramebot.config.command.TelegramBotCommand;
import com.helei.telegramebot.config.command.TelegramBotNameSpaceCommand;
import com.helei.telegramebot.dto.TGBotCommandContext;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
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
public abstract class AbstractTelegramBot extends TelegramLongPollingBot implements BaseCommandTelegramBot {


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
            CallbackQuery callbackQuery = update.getCallbackQuery();


            if (message != null) {
                // 判断当前菜单状态
                resolveMenuCommandHandler(message, true);

                // 1，消息
                messageHandler(message);
            } else if (callbackQuery != null) {
                // 2，callbackQuery

                resolveMenuCommandHandler(callbackQuery.getMessage(), false);

                callbackQueryHandler(callbackQuery);
            }
        });
    }


    /**
     * 处理菜单消息
     *
     * @param message message
     * @param invokeHandler invokeHandler
     */
    private void resolveMenuCommandHandler(Message message, boolean invokeHandler) {
        Result chatMenuState = telegramPersistenceService.getChatMenuState(botUsername, String.valueOf(message.getChatId()));
        if (chatMenuState != null && chatMenuState.getSuccess()) {
            TGMenuNode menuNode = (TGMenuNode) chatMenuState.getData();
            log.info("chatId[{}]当前菜单为[{}]", message.getChatId(), menuNode);

            // 命令消息取消掉咯
            if (invokeHandler && !message.getText().startsWith("/")) {
                BotApiMethod<?> botApiMethod = menuNode.menuCommandHandler(List.of(message.getText()), message);

                try {
                    execute(botApiMethod);
                } catch (TelegramApiException e) {
                    log.error("发送菜单命令handler的结果发生错误", e);
                }
            }
        }
    }

    /**
     * 处理callback（由键盘点击产生的）
     *
     * @param callbackQuery callbackQuery
     */
    private void callbackQueryHandler(CallbackQuery callbackQuery) {
        String callbackQueryData = callbackQuery.getData();

        Message message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        User from = message.getFrom();

        log.info("bot[{}] 收到消息 chatId[{}]-用户[{}] - callbackQuery[{}]", getBotUsername(), chatId, from.getUserName(), callbackQueryData);

        if (callbackQueryData.startsWith("/menu.")) {
            Result result = menuCommandHandler(callbackQueryData, message);

            if (result != null && !result.getSuccess()) {
                log.error("执行callbackQueryData[{}] 命令失败, {}",
                        callbackQueryData, result.getErrorMsg());
            }
        }
    }

    /**
     * 处理消息
     *
     * @param message 消息
     */
    private void messageHandler(Message message) {

        Result result = null;

        String chatId = String.valueOf(message.getChatId());

        try {
            User from = message.getFrom();
            String text = message.getText();

            log.info("bot[{}] 收到消息 chatId[{}]-用户[{}] - 消息[{}]", getBotUsername(), chatId, from.getUserName(), text);

            // 处理命令消息
            if (message.isCommand()) {
                //Step 1 解析命令、参数
                TGBotCommandContext commandContext = resolveCommand(text);

                String nameSpaceStr = commandContext.getNamespace();
                String commandStr = commandContext.getCommand();
                List<String> params = commandContext.getParams();

//                    String botName = commandAndBotName.length > 1 ? commandAndBotName[1] : "";
                //不是本机器人，不管
//                    if (!getBotUsername().equals(botName)) {
//                        return;
//                    }

                // 不是Namespace里的命令, 就是基础bot的命令
                if (StrUtil.isBlank(nameSpaceStr)) {
                    baseCommandHandler(commandStr, message);
                    return;
                }

                // 检查namespace 是否有对应的command
                if (!TelegramBotNameSpaceCommand.isContainCommand(nameSpaceStr, commandStr)) {
                    String format = String.format("不存在[%s]命令[%s]", nameSpaceStr, commandStr);
                    log.error(format);
                    result = Result.fail(format);
                    return;
                }

                TelegramBotNameSpaceCommand.NameSpace nameSpace = TelegramBotNameSpaceCommand.NameSpace.valueOf(nameSpaceStr);


                //Step 2 过滤
                if (commandMessageFilter(nameSpace, commandStr, params, message)) {//2 过滤
                    log.warn("bot[{}] 过滤掉 用户[{}] - 消息[{}]", getBotUsername(), from.getUserName(), text);
                } else {//3 其他命令
                    result = commandMessageHandler(nameSpace, commandStr, params, message);
                }
            } else {
                //处理普通消息
                result = normalMessageHandler(text, message);
            }
        } catch (Exception e) {
            log.error("处理消息[{}]出错", message, e);
        } finally {
            //Step 3 发送结果
            if (result != null) {
                resolveHandlerResult(chatId, result);
            }
        }
    }


    /**
     * 解析命令
     *
     * @param text text
     * @return String[]{nameSpaceStr, commandStr}
     */
    private TGBotCommandContext resolveCommand(String text) {
        String nameSpaceStr = "";
        String commandStr = "";

        String[] split = text.split(" ");

        String[] commandAndBotName = split[0].split("@");
        String[] nameSpaceAndCommand = commandAndBotName[0].replace("/", "").toUpperCase().split("\\.");

        if (nameSpaceAndCommand.length == 2) {
            nameSpaceStr = nameSpaceAndCommand[0].toUpperCase();
            commandStr = nameSpaceAndCommand[1].toUpperCase();
        } else if (nameSpaceAndCommand.length == 1) {
            commandStr = nameSpaceAndCommand[0].toUpperCase();
        }

        List<String> params = new ArrayList<>(Arrays.asList(split));
        params.removeFirst();

        return TGBotCommandContext
                .builder()
                .namespace(nameSpaceStr)
                .command(commandStr)
                .params(params)
                .build();
    }

    /**
     * 处理handler处理后的结果
     *
     * @param result result
     */
    private void resolveHandlerResult(String chatId, Result result) {
        try {
            if (BooleanUtil.isFalse(result.getSuccess())) {
                sendMessageToChat(chatId, result.getErrorMsg());
            } else if (result.getData() != null) {
                sendMessageToChat(chatId, result.getData().toString());
            }
        } catch (Exception e) {
            log.error("向chat[{}]发送结果[{}]时出现异常", chatId, result);
        }
    }


    /**
     * 处理基础命令
     *
     * @param commandStr 命令字符串
     * @param message    消息
     */
    private void baseCommandHandler(String commandStr, Message message) {

        TelegramBotCommand command = null;
        try {
            command = TelegramBotCommand.valueOf(commandStr);
        } catch (Exception e) {
            log.error("不存在基础命令[{}]", commandStr);
            sendMessageToChat(String.valueOf(message.getChatId()), String.format("不存在基础命令[%s]", commandStr));
            return;
        }

        switch (command) {
            case START -> startCommandHandler(message);
        }
    }


    /**
     * 菜单命令
     *
     * @param commandStr commandStr
     * @param message    message
     */
    public abstract Result menuCommandHandler(String commandStr, Message message);

    /**
     * 过滤命令消息
     *
     * @param nameSpace        nameSpace
     * @param nameSpaceCommand nameSpaceCommand
     * @param params           params
     * @param message          message
     */
    public abstract boolean commandMessageFilter(TelegramBotNameSpaceCommand.NameSpace nameSpace, String nameSpaceCommand, List<String> params, Message message);


    /**
     * 命令消息处理
     *
     * @param nameSpace        nameSpace
     * @param nameSpaceCommand 命令
     * @param params           参数
     * @param message          原消息内容
     */
    public abstract Result commandMessageHandler(TelegramBotNameSpaceCommand.NameSpace nameSpace, String nameSpaceCommand, List<?> params, Message message);


    /**
     * 普通消息处理
     *
     * @param messageText 消息文本
     * @param message     原消息内容
     */
    public abstract Result normalMessageHandler(String messageText, Message message);


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
     * @param chatId      chatId
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
