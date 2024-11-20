package com.helei.telegramebot.service.impl;

import com.helei.dto.base.Result;
import com.helei.telegramebot.bot.menu.TelegramBotMenuType;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import com.helei.telegramebot.util.TelegramRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;



@Slf4j
@Service
public class TelegramPersistenceServiceImpl implements ITelegramPersistenceService {

    @Autowired
    @Lazy
    private RedissonClient redissonClient;

    @Override
    public Result saveChatInBot(String botUsername, Long chatId, User user) {
        try {
            String key = TelegramRedisUtil.botListenChatIdSetKey(botUsername);
            RSet<String> set = redissonClient.getSet(key);
            set.add(chatId.toString());

            log.info("bot[{}]保存用户[{}]信息成功", botUsername, chatId);
        } catch (Exception e) {
            String errorMsg = String.format("保存连接用户[%s]-[%s]发生错误, %s", chatId, user.getUserName(), e.getMessage());
            log.error(errorMsg);
            return Result.fail(errorMsg);
        }
        return Result.ok();
    }

    @Override
    public Result isSavedChatInBot(String botUsername, Long chatId) {
        try {
            String key = TelegramRedisUtil.botListenChatIdSetKey(botUsername);
            RSet<String> set = redissonClient.getSet(key);
            if (set.contains(chatId.toString())) {
                return Result.ok();
            } else {
                return Result.fail(String.format("bot[%s]中没有chat[%s]的相关信息", botUsername, chatId));
            }
        } catch (Exception e) {
            String errorMsg = String.format("bot[%s]查询chat[%s]相关信息出错[%s]", botUsername, chatId, e.getMessage());
            log.error(errorMsg);
            return Result.fail(errorMsg);
        }
    }

    @Override
    public Result saveGroupChat(Chat chat) {
        return Result.ok();
    }

    @Override
    public Result saveChatMenuState(String botUsername, String chatId, TelegramBotMenuType menuType) {
        String name = menuType.getName();
        try {
            String key = TelegramRedisUtil.chatIdSolanaBotMenuKey(botUsername, chatId);

            redissonClient.getBucket(key).set(name);

            return Result.ok(name);
        } catch (Exception e) {
            String errorMsg = String.format("bot[%s]保存chat[%s]菜单状态[%s]出错[%s]", botUsername, chatId, name, e.getMessage());
            log.error(errorMsg);
            return Result.fail(errorMsg);
        }
    }

    @Override
    public Result getChatMenuState(String botUsername, String chatId) {
        try {
            String key = TelegramRedisUtil.chatIdSolanaBotMenuKey(botUsername, chatId);
            return Result.ok(redissonClient.getBucket(key).get());
        } catch (Exception e) {
            String errorMsg = String.format("bot[%s]获取chat[%s]菜单状态出错[%s]", botUsername, chatId, e.getMessage());
            log.error(errorMsg);
            return Result.fail(errorMsg);
        }
    }
}
