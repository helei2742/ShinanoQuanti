package com.helei.telegramebot.service.impl;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.Result;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


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
    public Result saveChatListenTradeSignal(String botUsername, String chatId, RunEnv runEnv, TradeType tradeType, CEXType cexType, List<String> symbols) {

        List<String> errorKey = new ArrayList<>();

        for (String symbol : symbols) {
            String key = null;
            try {
                key = TelegramRedisUtil.tradeSignalListenChatIdSetKey(botUsername, runEnv, tradeType, cexType, symbol);
                RSet<String> set = redissonClient.getSet(key);
                set.add(chatId);
            } catch (Exception e) {
                errorKey.add(key);
                log.error("保存[{}]到[{}]出错", chatId, key);
            }
        }

        if (errorKey.isEmpty()) {
            return Result.ok();
        } else {
            return Result.fail(String.format("监听信号[%s]出错", errorKey));
        }
    }

    @Override
    public Result queryTradeSignalListenedChatId(String botUsername, RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol) {
        String key = "";
        try {
            key = TelegramRedisUtil.tradeSignalListenChatIdSetKey(botUsername, runEnv, tradeType, cexType, symbol);

            Set<Object> chatIds = redissonClient.getSet(key).readAll();

            return Result.ok(chatIds, chatIds.size());
        } catch (Exception e) {
            return Result.fail(String.format("查询监听信号[%s]的chatId失败", key));
        }
    }
}
