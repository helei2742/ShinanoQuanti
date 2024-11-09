package com.helei.realtimedatacenter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.account.*;
import com.helei.dto.base.KeyValue;
import com.helei.realtimedatacenter.config.RealtimeConfig;
import com.helei.realtimedatacenter.manager.ExecutorServiceManager;
import com.helei.realtimedatacenter.service.UserService;
import com.helei.realtimedatacenter.supporter.BatchWriteSupporter;
import com.helei.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final RealtimeConfig realtimeConfig = RealtimeConfig.INSTANCE;


    @Autowired
    private BatchWriteSupporter batchWriteSupporter;

    @Autowired
    private BinanceBaseClientManager binanceBaseClientManager;

    private final ExecutorService executor;

    @Autowired
    public UserServiceImpl(ExecutorServiceManager executorServiceManager) {
        executor = executorServiceManager.getSyncTaskExecutor();
    }


    @Override
    public List<UserInfo> queryAll() {

        for (KeyValue<RunEnv, TradeType> keyValue : realtimeConfig.getRun_type().getRunTypeList()) {
            //TODO 查数据库, 只取这些环境里的

        }
        List<UserInfo> list = new ArrayList<>();

        UserInfo u_contract_test_net_account = UserInfo.builder()
                .id(1)
                .username("合约测试网账号")
                .password("123456")
                .accountInfos(List.of(
                        UserAccountInfo
                                .builder()
                                .id(1)
                                .userId(1)
                                .userAccountStaticInfo(
                                        UserAccountStaticInfo
                                                .builder()
                                                .id(1)
                                                .userId(1)
                                                .accountPositionConfig(AccountPositionConfig
                                                        .builder()
                                                        .riskPercent(0.5)
                                                        .leverage(10)
                                                        .build()
                                                )
                                                .asKey(new ASKey("b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66", "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f"))
                                                .subscribeSymbol(List.of("btcusdt", "ethusdt", "solusdt"))
                                                .runEnv(RunEnv.TEST_NET)
                                                .tradeType(TradeType.CONTRACT)
                                                .build()
                                )
                                .build()
                ))
                .build();
        UserInfo spot_test_net_account = UserInfo.builder()
                .id(2)
                .username("现货测试网账号")
                .password("123456")
                .accountInfos(List.of(
                        UserAccountInfo
                                .builder()
                                .id(2)
                                .userId(2)
                                .userAccountStaticInfo(
                                        UserAccountStaticInfo
                                                .builder()
                                                .id(2)
                                                .userId(2)
                                                .accountPositionConfig(AccountPositionConfig
                                                        .builder()
                                                        .riskPercent(0.5)
                                                        .leverage(10)
                                                        .build()
                                                )
                                                .subscribeSymbol(List.of("btcusdt", "ethusdt", "solusdt"))
                                                .asKey(new ASKey("1JIhkPyK07xadG9x8hIwqitN95MgpypPzA4b6TLraTonRnJ8BBJQlaO2iL9tPH0Y", "t84TYFR1zieMGncbw3kYq4zAPLxIJHJeMdD8V0FMKxij9fApojV6bhbDpyyjNDWt"))
                                                .runEnv(RunEnv.TEST_NET)
                                                .tradeType(TradeType.SPOT)
                                                .build()
                                )
                                .build()
                ))
                .build();

        UserInfo binance_account = UserInfo.builder()
                .id(3)
                .username("正式网账号")
                .password("123456")
                .accountInfos(List.of(
                        UserAccountInfo
                                .builder()
                                .id(3)
                                .userId(3)
                                .userAccountStaticInfo(
                                        UserAccountStaticInfo
                                                .builder()
                                                .id(3)
                                                .userId(3)
                                                .accountPositionConfig(AccountPositionConfig
                                                        .builder()
                                                        .riskPercent(0.5)
                                                        .leverage(10)
                                                        .build()
                                                )
                                                .subscribeSymbol(List.of("btcusdt", "ethusdt", "solusdt"))
                                                .asKey(new ASKey("TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz", "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU"))
                                                .runEnv(RunEnv.NORMAL)
                                                .tradeType(TradeType.SPOT)
                                                .build()
                                )
                                .build()
                ))
                .build();

        list.add(u_contract_test_net_account);
        list.add(spot_test_net_account);
        list.add(binance_account);
        return list;
    }

    @Override
    public List<UserInfo> queryEnvUser(RunEnv runEnv, TradeType tradeType) {
        //TODO 测试阶段，写死的

        return List.of();
    }

    @Override
    public void updateUserAccountRTInfo(RunEnv runEnv, TradeType tradeType, UserAccountRealTimeInfo realTimeInfo) {

        long accountId = realTimeInfo.getId();
        long userId = realTimeInfo.getUserId();

        String key = RedisKeyUtil.getUserAccountEnvRTDataHashKey(runEnv, tradeType, userId);
        String hashKey = String.valueOf(accountId);

        //只发实时的部分数据
        String value = JSONObject.toJSONString(realTimeInfo);

        log.debug("更新账户实时信息，key[{}], value[{}]", key, value);

        batchWriteSupporter.writeToRedisHash(key, hashKey, value);
    }

    @Override
    public void updateUserAccountStaticInfo(RunEnv runEnv, TradeType tradeType, UserAccountStaticInfo staticInfo) {
        long accountId = staticInfo.getId();
        long userId = staticInfo.getUserId();

        String key = RedisKeyUtil.getUserAccountEnvStaticDataHashKey(runEnv, tradeType, userId);
        String hashKey = String.valueOf(accountId);

        //只发实时的部分数据
        String value = JSONObject.toJSONString(staticInfo);

        log.debug("更新账户静态信息，key[{}], value[{}]", key, value);

        batchWriteSupporter.writeToRedisHash(key, hashKey, value);
    }


    /**
     * 更新UserInfo到Redis，包括User名下的账户信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public void updateEnvAllUserInfoToRedis(RunEnv env, TradeType tradeType) {
        List<UserInfo> userInfos = queryAll();
        try {
            BinanceWSReqRespApiClient requestClient = (BinanceWSReqRespApiClient) binanceBaseClientManager.getEnvTypedApiClient(env, tradeType, BinanceWSClientType.REQUEST_RESPONSE).get();

            //Step 1 遍历用户
            for (UserInfo userInfo : userInfos) {
                updateUserInfoTpRedis(env, tradeType, userInfo, requestClient);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新用户信息到redis
     *
     * @param env           env
     * @param tradeType     tradeType
     * @param userInfo      userInfo
     * @param requestClient requestClient
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     */
    private void updateUserInfoTpRedis(RunEnv env, TradeType tradeType, UserInfo userInfo, BinanceWSReqRespApiClient requestClient) throws InterruptedException, ExecutionException {
        Map<CompletableFuture<JSONObject>, UserAccountStaticInfo> futuresMap = new HashMap<>();
        Set<Long> accountIds = new HashSet<>();

        //Step 2 遍历用户下的账户静态数据，获取详细信息（实时数据）
        for (UserAccountInfo accountInfo : userInfo.getAccountInfos()) {
            UserAccountStaticInfo staticInfo = accountInfo.getUserAccountStaticInfo();

            if (!staticInfo.getRunEnv().equals(env) || !staticInfo.getTradeType().equals(tradeType)) {
                log.warn("userId[{}]-accountId[{}] 不能在当前环境[{}]-[{}]下运行", accountInfo.getUserId(), accountInfo.getId(), env, tradeType);
                continue;
            }

            CompletableFuture<JSONObject> accountStatusFuture = requestClient
                    .getAccountApi()
                    .accountStatus(staticInfo.getAsKey(), true)
                    .thenApplyAsync(jb -> {
                        // 记录成功同步信息的id
                        accountIds.add(accountInfo.getId());
                        return jb;
                    }, executor);
            futuresMap.put(accountStatusFuture, staticInfo);
        }

        //Step 3 解析详细信息（实时数据），放入UserAccountInfo，并写入redis
        CompletableFuture
                .allOf(futuresMap.keySet().toArray(new CompletableFuture[0]))
                .whenCompleteAsync((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("userId[{}}获取最新账户信息发生错误", userInfo.getId(), throwable);
                    }
                    futuresMap.forEach((future, staticInfo) -> {
                        long userId = staticInfo.getUserId();
                        long accountId = staticInfo.getId();

                        try {
                            JSONObject result = future.get();

                            log.info("获取到userId[{}]-accountId[{}]最新的账户信息 [{}]", userId, accountId, result);

                            //解析结构, 创建账户实时信息
                            UserAccountRealTimeInfo realTimeInfo = UserAccountRealTimeInfo.generateAccountStatusFromJson(result);
                            realTimeInfo.setUserId(userId);
                            realTimeInfo.setId(accountId);

                            //写redis
                            updateUserAccountStaticInfo(staticInfo.getRunEnv(), staticInfo.getTradeType(), staticInfo);
                            updateUserAccountRTInfo(staticInfo.getRunEnv(), staticInfo.getTradeType(), realTimeInfo);
                        } catch (InterruptedException | ExecutionException e) {
                            accountIds.remove(staticInfo.getId());
                            throw new RuntimeException(String.format("userId[%s]-accountId[%s]获取最新账户信息发生错误", userId, accountId), e);
                        }
                    });
                }, executor)
                .get();

        userInfo.setAccountIds(accountIds);
        log.info("userId[{}] 所有runEnv[{}]-tradeType[{}]的账户信息初始化完毕", userInfo.getId(), env, tradeType);


        //Step 4 User 数据写入Redis
        String key = RedisKeyUtil.getUserBaseInfoKey(env, tradeType, userInfo.getId());

        JSONObject jb = new JSONObject();
        jb.put("id", userInfo.getId());
        jb.put("username", userInfo.getUsername());
        jb.put("email", userInfo.getEmail());
        jb.put("accountIds", userInfo.getAccountIds());
        batchWriteSupporter.writeToRedis(key, jb.toString());
    }


    /**
     * 更新所有的用户信息到redis
     */
    @Override
    public void updateAllUserInfo() {
        for (KeyValue<RunEnv, TradeType> keyValue : realtimeConfig.getRun_type().getRunTypeList()) {
            updateEnvAllUserInfoToRedis(keyValue.getKey(), keyValue.getValue());
        }
    }
}
