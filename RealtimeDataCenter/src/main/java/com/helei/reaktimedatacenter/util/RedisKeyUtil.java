package com.helei.reaktimedatacenter.util;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;

public class RedisKeyUtil {

    private static final String USER_ACCOUNT_PREFIX = "user:account";

    public static String getUserAccountInfoKey(long userId, long accountId, RunEnv runEnv, TradeType tradeType) {

        return USER_ACCOUNT_PREFIX + ":" + userId + ":" + accountId + ":" + runEnv + ":" + tradeType;
    }
}
