package com.helei.tradeapplication.service;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.UserAccountInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserAccountInfoService {


    /**
     * 查询指定环境下的所有账户信息
     *
     * @param env       env
     * @param tradeType 交易类型
     * @return 账户信息列表
     */
    CompletableFuture<List<UserAccountInfo>> queryEnvAccountInfo(RunEnv env, TradeType tradeType);


    /**
     * 查询指定环境下指定账户id的账户信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     * @param accountId 账户id
     * @return 账户信息
     */
    CompletableFuture<AccountRTData> queryAccountRTInfo(RunEnv env, TradeType tradeType, long accountId);
}
