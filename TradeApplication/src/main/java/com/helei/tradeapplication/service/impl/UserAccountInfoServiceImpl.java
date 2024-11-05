package com.helei.tradeapplication.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.UserAccountInfo;
import com.helei.tradeapplication.cache.UserInfoCache;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.UserAccountInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class UserAccountInfoServiceImpl implements UserAccountInfoService {

    private final ExecutorService executor;

    @Autowired
    private UserInfoCache userInfoCache;

    public UserAccountInfoServiceImpl(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getQueryExecutor();
    }


    public CompletableFuture<List<UserAccountInfo>> queryEnvAccountInfo(RunEnv env, TradeType tradeType) {
        return null;
    }

    @Override
    public CompletableFuture<AccountRTData> queryAccountRTInfo(RunEnv env, TradeType tradeType, long accountId) {
        return CompletableFuture.supplyAsync(()->userInfoCache.queryAccountRTData(env, tradeType, accountId), executor);
    }

}
