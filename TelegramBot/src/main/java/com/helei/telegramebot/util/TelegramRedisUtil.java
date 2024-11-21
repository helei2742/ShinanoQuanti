package com.helei.telegramebot.util;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;

public class TelegramRedisUtil {

    private static final String REDIS_KEY_PREFIX = "telegram:bot:";

    /**
     * 监听交易信号的chatId的redis key，set类型
     *
     * @param botUsername 机器人用户名
     * @param runEnv      runEnv
     * @param tradeType   tradeType
     * @param cexType     cexType
     * @param symbol      symbol
     * @return key
     */
    public static String tradeSignalListenChatIdSetKey(String botUsername, RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol, String signalName) {
        return REDIS_KEY_PREFIX + botUsername + ":signal:" + cexType.name() + ":" + runEnv.name() + ":" + tradeType.name() + ":" + symbol + ":" + signalName;
    }


    /**
     * bot关注的chatId的redis key， set类型
     *
     * @param botUsername 机器人名字
     * @return key
     */
    public static String botListenChatIdSetKey(String botUsername) {
        return REDIS_KEY_PREFIX + botUsername + ":listenChatIdSet";
    }

    /**
     * chatId solana钱包信息的前缀
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return key
     */
    public static String chatIdSolanaWalletPrefix(String botUsername, String chatId) {
        return REDIS_KEY_PREFIX + botUsername + "solana:wallet:" + chatId + ":";
    }

    /**
     * chatId solana钱包，追踪地址的key
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return key
     */
    public static String chatIdSolanaWalletTraceHashKey(String botUsername, String chatId) {
        return chatIdSolanaWalletPrefix(botUsername, chatId) + "trace";
    }


    /**
     * chatId solana钱包地址
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return key
     */
    public static String chatIdSolanaWalletInfoHashKey(String botUsername, String chatId) {
        return chatIdSolanaWalletPrefix(botUsername, chatId) + "info";
    }


    /**
     * chatId 默认的搜懒啊钱包地址
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return key
     */
    public static String chatIdSolanaWalletDefaultAddressKey(String botUsername, String chatId) {
        return chatIdSolanaWalletPrefix(botUsername, chatId) + "defaultWallet";
    }

    /**
     * 获取菜单状态
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return key
     */
    public static String chatIdSolanaBotMenuKey(String botUsername, String chatId) {
        return chatIdSolanaWalletPrefix(botUsername, chatId) + "menu";
    }


    /**
     * chatId的分布式锁
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return key
     */
    public static String chatIdLockKey(String botUsername, String chatId) {
        return chatIdSolanaWalletPrefix(botUsername, chatId) + "lock";
    }
}
