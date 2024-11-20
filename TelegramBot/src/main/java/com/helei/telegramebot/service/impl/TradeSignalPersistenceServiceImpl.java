package com.helei.telegramebot.service.impl;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.Result;
import com.helei.telegramebot.service.ITradeSignalPersistenceService;
import com.helei.telegramebot.util.TelegramRedisUtil;
import org.redisson.api.RKeys;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TradeSignalPersistenceServiceImpl implements ITradeSignalPersistenceService {

    @Autowired
    @Lazy
    private RedissonClient redissonClient;

    @Override
    public Result saveChatListenTradeSignal(String botUsername, String chatId, RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol, String signalName) {
        String key = TelegramRedisUtil.tradeSignalListenChatIdSetKey(botUsername, runEnv, tradeType, cexType, symbol, signalName);
        RSet<String> set = redissonClient.getSet(key);
        set.add(chatId);
        return Result.ok();
    }



    @Override
    public Result queryTradeSignalListenedChatId(String botUsername, RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol) {
        String keyPattern = "";
        Set<String> result = new HashSet<>();

        try {
            keyPattern = TelegramRedisUtil.tradeSignalListenChatIdSetKey(botUsername, runEnv, tradeType, cexType, symbol, "*");

            RKeys keys = redissonClient.getKeys();

            keys.getKeysStreamByPattern(keyPattern).forEach(key -> {
                RSet<String> set = redissonClient.getSet(key);
                result.addAll(set.readAll());
            });

            return Result.ok(result, result.size());
        } catch (Exception e) {
            return Result.fail(String.format("查询监听信号[%s]的chatId失败", keyPattern));
        }
    }
}

