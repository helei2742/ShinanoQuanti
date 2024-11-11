package com.helei.tradeapplication.cache;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.*;
import com.helei.dto.base.KeyValue;
import com.helei.tradeapplication.config.TradeAppConfig;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


@Slf4j
@Component
public class UserInfoCache {

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
     * @param userId    用户id
     * @param accountId 账户id
     * @return 实时数据
     */
    public CompletableFuture<UserAccountRealTimeInfo> queryAccountRTInfoFromRedis(RunEnv env, TradeType tradeType, long userId, long accountId) {
        return CompletableFuture.supplyAsync(() -> {
            String accountRTDataKey = RedisKeyUtil.getUserAccountEnvRTDataHashKey(env, tradeType, userId);

            RMap<String, String> rtMap = redissonClient.getMap(accountRTDataKey);

            // json手动解析
            JSONObject jsonObject = JSONObject.parseObject(rtMap.get(String.valueOf(accountId)));

            UserAccountRealTimeInfo userAccountRealTimeInfo = jsonObject.toJavaObject(UserAccountRealTimeInfo.class);


            // 解析资金信息
            JSONObject balancesJson = jsonObject.getJSONObject("accountBalanceInfo").getJSONObject("balances");
            List<BalanceInfo> balanceInfos = balancesJson.values().stream().map(o -> ((JSONObject) o).toJavaObject(BalanceInfo.class)).toList();
            userAccountRealTimeInfo.getAccountBalanceInfo().updateBalanceInfos(balanceInfos);


            //解析仓位信息
            JSONObject positionJson = jsonObject.getJSONObject("accountPositionInfo").getJSONObject("positions");
            List<PositionInfo> positionInfos = positionJson.values().stream().map(o -> ((JSONObject) o).toJavaObject(PositionInfo.class)).toList();
            userAccountRealTimeInfo.getAccountPositionInfo().updatePositionInfos(positionInfos);

            return userAccountRealTimeInfo;
        }, executor);
    }

    /**
     * 获取账户的静态数据，包含仓位设置、askey等
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     * @param userId    用户id
     * @param accountId 账户id
     * @return 静态信息
     */
    public CompletableFuture<UserAccountStaticInfo> queryAccountStaticInfoFromRedis(RunEnv env, TradeType tradeType, long userId, long accountId) {
        return CompletableFuture.supplyAsync(() -> {
            String staticDataHashKey = RedisKeyUtil.getUserAccountEnvStaticDataHashKey(env, tradeType, userId);

            RMap<String, String> staticMap = redissonClient.getMap(staticDataHashKey);

            return JSONObject.parseObject(staticMap.get(String.valueOf(accountId)), UserAccountStaticInfo.class);
        }, executor);
    }

    /**
     * 从redis查指定环境的所有用户信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public int queryAllUserBaseFromRedis(RunEnv env, TradeType tradeType, BiConsumer<String, UserInfo> consumer) {
        // Step 1 获取用户的pattern
        String accountPattern = RedisKeyUtil.getUserBaseInfoPattern(env, tradeType);
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
     * 查询用户基础信息，从redis。
     * 不包含账户相关信息
     *
     * @param userRedisKey userRedisKey
     * @return 用户基础信息
     */
    public CompletableFuture<UserInfo> queryUserBaseFromRedis(String userRedisKey) {
        return CompletableFuture.supplyAsync(() -> {

            RBucket<String> bucket = redissonClient.getBucket(userRedisKey);

            return JSONObject.parseObject(bucket.get(), UserInfo.class);
        }, executor);
    }


    /**
     * 查询用户的账户信息，直接写入userInfo参数的对应属性中
     *
     * @param userRedisKey userRedisKey
     * @param userInfo     userInfo
     * @param env          运行环境
     * @param type         交易类型
     */
    public CompletableFuture<UserInfo> queryUserAccountInfoFromRedis(String userRedisKey, UserInfo userInfo, RunEnv env, TradeType type) {
        userInfoCache.put(userRedisKey, userInfo);

        long userId = userInfo.getId();

        userInfo.setAccountInfos(new ArrayList<>());
        List<CompletableFuture<Void>> accountFutures = new ArrayList<>();

        //Step 3 根据账户id从redis查账户信息，并更新map
        for (Long accountId : userInfo.getAccountIds()) {

            //Step 3.1 查询静态信息
            CompletableFuture<UserAccountStaticInfo> staticFuture = queryAccountStaticInfoFromRedis(env, type, userId, accountId);

            //Step 3.2 查询动态信息
            CompletableFuture<UserAccountRealTimeInfo> realtimeFuture = queryAccountRTInfoFromRedis(env, type, userId, accountId);

            //Step 3.3 更新 accountInfoCache
            CompletableFuture<Void> accountFuture = staticFuture.thenAcceptBothAsync(realtimeFuture, (staticInfo, realTimeInfo) -> {

                UserAccountInfo userAccountInfo = accountInfoCache.get(env).get(type).compute(accountId, (k1, v1) -> {
                    if (v1 == null) {
                        v1 = new UserAccountInfo();
                        v1.setUserId(userId);
                        v1.setId(accountId);
                    }
                    v1.setUserAccountStaticInfo(staticInfo);
                    v1.setUserAccountRealTimeInfo(realTimeInfo);

                    return v1;
                });
                userInfo.getAccountInfos().add(userAccountInfo);

                log.info("env[{}]-tradeType[{}]-userId[{}]-accountId[{}]信息同步到cache完成", env, type, userId, accountId);
            }, executor);

            accountFutures.add(accountFuture);
        }

        return CompletableFuture
                .allOf(accountFutures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(Void -> userInfo);
    }

    /**
     * 更新用户账户信息缓存
     *
     * @throws ExecutionException   ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public void updateUserBaseAndRTInfoFromRedis() throws ExecutionException, InterruptedException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        /*
         * 获取账户信息，不包括实时的资金信息和仓位信息
         */
        List<KeyValue<RunEnv, TradeType>> runTypeList = tradeAppConfig.getRun_type().getRunTypeList();
        for (KeyValue<RunEnv, TradeType> keyValue : runTypeList) {

            //Step 1 遍历环境
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                RunEnv env = keyValue.getKey();
                TradeType type = keyValue.getValue();

                accountInfoCache.putIfAbsent(env, new ConcurrentHashMap<>());
                accountInfoCache.get(env).putIfAbsent(type, new ConcurrentHashMap<>());

                log.info("开始初始化环境env[{}]-tradeType[{}]的用户信息", env, type);

                //Step 2 从redis查询UserBaseInfo
                List<CompletableFuture<UserInfo>> accountFutures = new ArrayList<>();
                int total = queryAllUserBaseFromRedis(env, type, (k, userInfo) -> {

                    //Step 3 再从redis查询用户账户相关信息
                    CompletableFuture<UserInfo> accountFuture = queryUserAccountInfoFromRedis(k, userInfo, env, type);
                    accountFutures.add(accountFuture);
                });

                try {
                    CompletableFuture
                            .allOf(accountFutures.toArray(new CompletableFuture[0]))
                            .whenCompleteAsync((unused, throwable) -> {
                                if (throwable != null) {
                                    log.error("获取env[{}]-tradeType[{}]的用户信息出错", env, type, throwable);
                                } else {
                                    log.info("环境env[{}]-tradeType[{}]的用户信息初始化完毕, 共[{}]个用户", env, type, total);
                                }
                            })
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenCompleteAsync((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("更新用户信息时发生错误", throwable);
                        System.exit(-1);
                    }
                    log.info("所有运行环境的用户信息初始化完毕, 环境列表: [{}]", runTypeList);
                }).get();
    }

    /**
     * 从redis查询用户信息
     * 查询出的用户信息会从新放入缓存
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     * @param userId    用户id
     * @return CompletableFuture<UserInfo>
     */
    public CompletableFuture<UserInfo> queryUserInfoFromRedis(RunEnv env, TradeType tradeType, long userId) {
        String key = RedisKeyUtil.getUserBaseInfoKey(env, tradeType, userId);

        return queryUserBaseFromRedis(key)
                .thenApplyAsync(userInfo -> {
                    CompletableFuture<UserInfo> future = queryUserAccountInfoFromRedis(key, userInfo, env, tradeType);
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(String.format("查询[%s]-[%s}-[%s]的redis账户数据出错", env.name(), tradeType.name(), userId), e);
                    }
                });
    }


    /**
     * 从cache获取账户信息
     *
     * @param env       env
     * @param tradeType tradeType
     * @param accountId accountId
     * @return UserAccountInfo
     */
    public UserAccountInfo queryAccountInfoFromCache(RunEnv env, TradeType tradeType, long accountId) {
        ConcurrentMap<TradeType, ConcurrentMap<Long, UserAccountInfo>> map1 = accountInfoCache.get(env);
        if (map1 == null) return null;

        ConcurrentMap<Long, UserAccountInfo> map2 = map1.get(tradeType);
        if (map2 == null) return null;

        return map2.get(accountId);
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


}

