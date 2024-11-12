package com.helei.telegramebot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class ShinanoTelegramBot extends TelegramLongPollingBot {

    private final String botUsername;

    private final String token;

    public ShinanoTelegramBot(String botUsername, String token) {
        this.botUsername = botUsername;
        this.token = token;
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
        Message message = update.getMessage();
        User from = message.getFrom();

        log.info("bot[{}] 收到消息 用户[{}] - 消息[{}]", getBotUsername(), from.getUserName(), message.getText());
    }


    // 给指定群组发送消息的方法
    public void sendMessageToGroup(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);  // 发送消息
        } catch (TelegramApiException e) {
            log.error("给群组 [{}] 发送消息发生错误", chatId, e);
        }
    }

    // 获取特定成员信息
    public void getChatMemberInfo(String chatId, long userId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId);
        getChatMember.setUserId(userId);

        try {
            ChatMember chatMember = execute(getChatMember);
            System.out.println("User Status: " + chatMember.getStatus()); // 打印成员状态
            // 根据需要获取更多信息，例如用户名、权限等
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println("Failed to get chat member info: " + e.getMessage());
        }
    }

    // 获取群组管理员列表
    public void getChatAdministratorsInfo(String chatId) {
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);

        try {
            List<ChatMember> administrators = execute(getChatAdministrators);
            for (ChatMember admin : administrators) {
                System.out.println("Admin ID: " + admin.getUser().getId());
                System.out.println("Admin Username: " + admin.getUser().getUserName());
                System.out.println("Admin Status: " + admin.getStatus());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println("Failed to get chat administrators info: " + e.getMessage());
        }
    }
}
