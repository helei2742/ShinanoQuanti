package com.helei.telegramebot.util;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;

public class TelegramRedisUtil {

    private static final String REDIS_KEY_PREFIX = "telegram:bot:";

    /**
     * 监听交易信号的chatId的redis key，set类型
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @param cexType   cexType
     * @param symbol    symbol
     * @return key
     */
    public static String tradeSignalListenChatIdSetKey(RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol) {
        return REDIS_KEY_PREFIX + "signal:" + cexType.name() + ":" + runEnv.name() + ":" + tradeType.name() + ":" + symbol;
    }
}
