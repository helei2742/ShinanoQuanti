package com.helei.tradeapplication.cache;


import com.alibaba.fastjson.JSONObject;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;
import com.helei.dto.base.KeyValue;
import com.helei.tradeapplication.config.TradeAppConfig;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


@Slf4j
@Component
public class UserInfoCache implements InitializingBean {

    private final TradeAppConfig tradeAppConfig = TradeAppConfig.INSTANCE;

    private final ExecutorService executor;


    /**
     * 用户信息缓存， 放基础的数据，不会放实时的仓位和资金信息
     */
    private final ConcurrentMap<String, UserInfo> userInfoCache = new ConcurrentHashMap<>();

    /**
     * 账户信息缓存, account:UserAccountInfo
     */
    private final ConcurrentMap<RunEnv, ConcurrentMap<TradeType, ConcurrentMap<Long, UserAccountInfo>>> accountInfoCache = new ConcurrentHashMap<>();


    @Autowired
    private RedissonClient redissonClient;

    public UserInfoCache(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getQueryExecutor();
    }


    /**
     * 获取账户的实时数据，包含资金和仓位信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     * @param accountId 账户id
     * @return 实时数据
     */
    public AccountRTData queryAccountRTData(RunEnv env, TradeType tradeType, long accountId) {
        String accountRTDataKey = RedisKeyUtil.getUserAccountEnvRTDataKey(env, tradeType);

        RMap<String, String> rtMap = redissonClient.getMap(accountRTDataKey);

        return JSONObject.parseObject(rtMap.get(String.valueOf(accountId)), AccountRTData.class);
    }


    /**
     * 从本地缓存中查询指定环境的用户信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     * @return List<UserInfo>
     */
    public List<UserAccountInfo> queryAllAccountInfoFromCache(RunEnv env, TradeType tradeType) {
        ConcurrentMap<TradeType, ConcurrentMap<Long, UserAccountInfo>> map1 = accountInfoCache.get(env);
        if (map1 != null) {
            ConcurrentMap<Long, UserAccountInfo> map2 = map1.get(tradeType);
            if (map2 != null) {
                return map2.values().stream().toList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * 从redis查指定环境的所有用户信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public int queryAllUserInfoFromRemote(RunEnv env, TradeType tradeType, BiConsumer<String, UserInfo> consumer) {
        // Step 1 获取用户的pattern
        String accountPattern = RedisKeyUtil.getUserInfoPattern(env, tradeType);
        RKeys keys = redissonClient.getKeys();

        // Step 2 用pattern筛选key， 再查对应key下的UserInfo
        AtomicInteger total = new AtomicInteger();
        keys.getKeysStreamByPattern(accountPattern).forEach(key -> {
            RBucket<String> bucket = redissonClient.getBucket(key);
            UserInfo userInfo = JSONObject.parseObject(bucket.get(), UserInfo.class);

            consumer.accept(key, userInfo);
            total.getAndIncrement();
        });
        return total.get();
    }


    /**
     * 更新用户账户信息缓存
     *
     * @throws ExecutionException   ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public void updateUserInfo() throws ExecutionException, InterruptedException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        /*
         * 获取账户信息，不包括实时的资金信息和仓位信息
         */
        for (KeyValue<RunEnv, TradeType> keyValue : tradeAppConfig.getRun_type().getRunTypeList()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                RunEnv env = keyValue.getKey();
                accountInfoCache.putIfAbsent(env, new ConcurrentHashMap<>());
                TradeType type = keyValue.getValue();
                accountInfoCache.get(env).put(type, new ConcurrentHashMap<>());

                log.info("开始初始化环境env[{}]-tradeType[{}]的账户信息", env, type);

                int total = queryAllUserInfoFromRemote(env, type, (k, v) -> {
                    for (UserAccountInfo accountInfo : v.getAccountInfos()) {

                        // 更新 accountInfoCache
                        accountInfoCache.get(env).get(type).compute(accountInfo.getId(), (k1,v1)->{
                            if (v1 == null) {
                                v1 = accountInfo;
                            }
                            return v1;
                        });
                    }
                    userInfoCache.put(k, v);
                });

                log.info("环境env[{}]-tradeType[{}]的账户信息初始化完毕, 共[{}]个账户", env, type, total);
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenCompleteAsync((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("更新用户信息时发生错误", throwable);
                        System.exit(-1);
                    }
                }).get();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        updateUserInfo();
    }

}

