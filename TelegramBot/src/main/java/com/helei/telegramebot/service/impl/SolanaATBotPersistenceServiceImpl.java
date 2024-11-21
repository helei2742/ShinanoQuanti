package com.helei.telegramebot.service.impl;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.helei.dto.base.Result;
import com.helei.solanarpc.dto.SolanaAddress;
import com.helei.solanarpc.util.SolanaKeyAddressUtil;
import com.helei.telegramebot.entity.ChatDefaultWallet;
import com.helei.telegramebot.entity.ChatWallet;
import com.helei.telegramebot.service.IChatDefaultWalletService;
import com.helei.telegramebot.service.IChatWalletService;
import com.helei.telegramebot.service.ISolanaATBotPersistenceService;
import com.helei.telegramebot.util.TelegramRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * TODO 加redis缓存！
 */
@Slf4j
@Service
public class SolanaATBotPersistenceServiceImpl implements ISolanaATBotPersistenceService {

    @Lazy
    @Autowired
    private RedissonClient redissonClient;

    @Lazy
    @Autowired
    private ISolanaATBotPersistenceService solanaATBotPersistenceService;

    @Autowired
    private IChatWalletService chatWalletService;


    @Autowired
    private IChatDefaultWalletService chatDefaultWalletService;


    @Override
    @Transactional
    public Result bindWalletByPrivateKey(String botUsername, String chatId, String privateKey) {
        try {
            // 1.计算获取公匙和密匙
            Pair<String, String> psKeyPair = SolanaKeyAddressUtil.getPSKeyFromPrivateKey(privateKey);
            String pubKey = psKeyPair.getKey();
            String secretKey = psKeyPair.getValue();

            ChatWallet query = new ChatWallet();
            query.setChatId(chatId);
            query.setPublicKey(pubKey);
            ChatWallet one = chatWalletService.getBaseMapper().selectOne(new QueryWrapper<>(query));

            // 2.1绑定过该地址
            if (one != null) {
                return Result.fail("已绑定过钱包地址[" + pubKey.substring(0, 8) + "...]");
            } else {
                // 2.2 没有绑定过
                ChatWallet chatWallet = ChatWallet.builder().publicKey(pubKey).privateKey(privateKey).secretKey(secretKey).chatId(chatId).build();

                // 保存数据库
                chatWalletService.save(chatWallet);

                // 更新默认钱包地址
                solanaATBotPersistenceService.updateDefaultWalletAddress(botUsername, chatId, pubKey, false);

                return Result.ok();
            }
        } catch (Exception e) {
            String format = String.format("绑定chatId[%s]的钱包失败, %s", chatId, e.getMessage());
            log.error(format, e);
            return Result.fail(format);
        }
    }

    @Override
    @Transactional
    public void updateDefaultWalletAddress(String botUsername, String chatId, String pubKey, boolean isCover) {
        ChatDefaultWallet byId = chatDefaultWalletService.getById(chatId);
        if (byId == null || isCover) {
            ChatDefaultWallet chatDefaultWallet = new ChatDefaultWallet(chatId, pubKey);
            chatDefaultWalletService.save(chatDefaultWallet);
        }
    }


    @Override
    public Result updateChatListenAddress(String botUsername, String chatId, SolanaAddress solanaAddress) {
        try {
            String key = TelegramRedisUtil.chatIdSolanaWalletTraceHashKey(chatId, botUsername);

            RMap<String, String> map = redissonClient.getMap(key);

            map.put(solanaAddress.getAccountAddress(), JSONObject.toJSONString(solanaAddress));

            return Result.ok();
        } catch (Exception e) {
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

    @Override
    public String queryChatIdDefaultWalletAddress(String botUsername, String chatId) {
        ChatDefaultWallet byId = chatDefaultWalletService.getById(chatId);

        if (byId == null) return null;

        return byId.getPublicKey();
    }

    @Override
    public List<ChatWallet> queryChatIdAllWallet(String botUsername, String chatId) {
        ChatWallet query = new ChatWallet();
        query.setChatId(chatId);

        return chatWalletService.getBaseMapper().selectList(new QueryWrapper<>(query));
    }

    @Override
    public ChatWallet queryChatIdWallet(ChatWallet query) {

        return chatWalletService.getBaseMapper().selectOne(new QueryWrapper<>(query));
    }

    @Override
    public boolean saveChatWallet(ChatWallet chatWallet) {

        return chatWalletService.save(chatWallet);
    }
}

