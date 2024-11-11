package com.helei.util;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;


public class RedisKeyUtil {

    private static final String USER_Info_PREFIX = "user";

    /**
     * Redis中存放相应环境数据的前缀
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return prefix
     */
    public static String getEnvKeyPrefix(RunEnv runEnv, TradeType tradeType) {
        return (runEnv.name() + ":" + tradeType.name() + ":").toLowerCase();
    }

    /**
     * Redis中存放用户数据的前缀
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return prefix
     */
    public static String getUserEnvKeyPrefix(RunEnv runEnv, TradeType tradeType) {
        return getEnvKeyPrefix(runEnv, tradeType) + USER_Info_PREFIX + ":";
    }

    /**
     * redis中存放用户具体数据的前缀
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return prefix
     */
    public static String getUserInfoKeyPrefix(RunEnv runEnv, TradeType tradeType) {
        return getUserEnvKeyPrefix(runEnv, tradeType) + "id:";
    }

    /**
     * redis中存放用户具体数据的匹配模式
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return pattern
     */
    public static String getUserBaseInfoPattern(RunEnv runEnv, TradeType tradeType) {
        return getUserInfoKeyPrefix(runEnv, tradeType) + "*base*";
    }

    /**
     * 获取用户账户实时数据的key
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return String
     */
    public static String getUserAccountEnvRTDataHashKey(RunEnv runEnv, TradeType tradeType, long userId) {
        return getUserInfoKeyPrefix(runEnv, tradeType) + userId + ":realtime_account_data";
    }

    /**
     * 获取用户账户历史数据的key
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return String
     */
    public static String getUserAccountEnvStaticDataHashKey(RunEnv runEnv, TradeType tradeType, long userId) {
        return getUserInfoKeyPrefix(runEnv, tradeType) + userId + ":static_account_data";
    }


    /**
     * 用户基础数据的key
     *
     * @param env       env
     * @param tradeType tradeType
     * @param userId    userId
     * @return key
     */
    public static String getUserBaseInfoKey(RunEnv env, TradeType tradeType, long userId) {
        return getUserInfoKeyPrefix(env, tradeType) + userId + ":base";
    }

}

