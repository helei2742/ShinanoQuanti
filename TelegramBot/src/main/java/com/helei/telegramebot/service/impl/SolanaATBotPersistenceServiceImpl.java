package com.helei.telegramebot.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.dto.base.Result;
import com.helei.solanarpc.dto.SolanaAddress;
import com.helei.telegramebot.service.ISolanaATBotPersistenceService;
import com.helei.telegramebot.util.TelegramRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SolanaATBotPersistenceServiceImpl implements ISolanaATBotPersistenceService {

    @Lazy
    @Autowired
    private RedissonClient redissonClient;



    @Override
    public Result bindWalletAddress(String botUsername, String chatId, String address) {
        try {
            String key = TelegramRedisUtil.chatIdSolanaWalletInfoHashKey(chatId, botUsername);

            RMap<String, String> map = redissonClient.getMap(key);

            if (map.containsKey(address)) {
                return Result.fail("已绑定过钱包地址[" + address.substring(0, 8) + "]");
            } else {
                // TODO 可以存对象，放钱包信息
                map.put(address, "");
                return Result.ok();
            }
        } catch (Exception e) {
            String format = String.format("绑定chatId[%s]的钱包地址失败, %s", chatId, e.getMessage());
            log.error(format, e);
            return Result.fail(format);
        }
    }



    @Override
    public Result updateChatListenAddress(String botUsername, String chatId, SolanaAddress solanaAddress) {
        try {
            String key = TelegramRedisUtil.chatIdSolanaWalletTraceHashKey(chatId, botUsername);

            RMap<String, String> map = redissonClient.getMap(key);

            map.put(solanaAddress.getAccountAddress(), JSONObject.toJSONString(solanaAddress));

            return Result.ok();
        } catch (Exception e){
            String format = String.format("更新chatId[%s]跟踪的地址信息失败, %s", chatId, e.getMessage());
            log.error(format, e);
            return Result.fail(format);
        }
    }

    @Override
    public Result deleteChatListenAddress(String botUsername, String chatId, String solanaAddress) {

        try {
            String key = TelegramRedisUtil.chatIdSolanaWalletTraceHashKey(chatId, botUsername);
            RMap<String, String> map = redissonClient.getMap(key);
            map.remove(solanaAddress);
            return Result.ok();
        } catch (Exception e) {
            String format = String.format("删除chatId[%s]跟踪的地址信息失败, %s", chatId, e.getMessage());
            log.error(format, e);
            return Result.fail(format);
        }
    }
}

