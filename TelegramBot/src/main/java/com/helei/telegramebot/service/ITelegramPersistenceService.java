package com.helei.telegramebot.service;

import com.helei.dto.base.Result;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;


public interface ITelegramPersistenceService {


    /**
     * 保存聊天用户信息
     *
     * @param botUsername 机器人用户名
     * @param chatId      聊天id
     * @param user        用户
     * @return 是否保存成功
     */
    Result saveChatInBot(String botUsername, Long chatId, User user);


    /**
     * 查询指定的chatId是否被注册过
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return 是否被注册过
     */
    Result isSavedChatInBot(String botUsername, Long chatId);


    /**
     * 保存群组信息
     *
     * @param chat 群组信息
     * @return 是否成功
     */
    Result saveGroupChat(Chat chat);


    /**
     * 保存聊天菜单状态
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @param menuType    menuType
     * @return Result
     */
    Result saveChatMenuState(String botUsername, String chatId, TGMenuNode menuType);


    /**
     * 获取菜单状态
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return Result
     */
    Result getChatMenuState(String botUsername, String chatId);
}

