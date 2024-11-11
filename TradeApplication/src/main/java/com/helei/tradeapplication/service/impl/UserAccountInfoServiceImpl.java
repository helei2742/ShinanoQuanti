package com.helei.tradeapplication.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.UserAccountInfo;
import com.helei.tradeapplication.cache.UserInfoCache;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.UserAccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class UserAccountInfoServiceImpl implements UserAccountInfoService {

    private final ExecutorService executor;

    @Autowired
    private UserInfoCache userInfoCache;

    public UserAccountInfoServiceImpl(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getQueryExecutor();
    }

    @Override
    public CompletableFuture<List<UserAccountInfo>> queryEnvAccountInfo(RunEnv env, TradeType tradeType) {
        return CompletableFuture.supplyAsync(() -> userInfoCache.queryAllAccountInfoFromCache(env, tradeType), executor);
    }

    @Override
    public CompletableFuture<UserAccountInfo> queryAccountNewInfo(RunEnv env, TradeType tradeType, long userId, long accountId) {
        //Step 1 从缓存拿基础信息
        UserAccountInfo userAccountInfo = userInfoCache.queryAccountInfoFromCache(env, tradeType, accountId);

        if (userAccountInfo == null) {
            log.warn("ent[{}]-tradeType[{}]-accountId[{}] 从缓存获取账户信息失败", env, tradeType, accountId);
            //从redis拿一次
            return userInfoCache.queryUserInfoFromRedis(env, tradeType, userId)
                    .thenApplyAsync(userInfo -> userInfoCache.queryAccountInfoFromCache(env, tradeType, accountId));
        } else {
            //Step 2 从redis拿实时信息
            return userInfoCache.queryAccountRTInfoFromRedis(env, tradeType, userId, accountId).thenApplyAsync(rtInfo -> {
                userAccountInfo.setUserAccountRealTimeInfo(rtInfo);
                return userAccountInfo;
            });
        }
    }
}

