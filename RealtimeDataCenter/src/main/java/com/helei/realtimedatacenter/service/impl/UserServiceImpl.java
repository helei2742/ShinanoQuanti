package com.helei.realtimedatacenter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.account.AccountPositionConfig;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;
import com.helei.dto.base.KeyValue;
import com.helei.realtimedatacenter.config.RealtimeConfig;
import com.helei.realtimedatacenter.service.UserService;
import com.helei.realtimedatacenter.supporter.BatchWriteSupporter;
import com.helei.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class UserServiceImpl implements UserService, InitializingBean {

    private final RealtimeConfig realtimeConfig = RealtimeConfig.INSTANCE;

    @Autowired
    private BatchWriteSupporter batchWriteSupporter;


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
                ))
                .build();


        list.add(u_contract_test_net_account);
        list.add(spot_test_net_account);
        list.add(binance_account);
        return list;
    }


    /**
     * 更新用户账户信息
     *
     * @param userAccountInfo userAccountInfo
     */
    @Override
    public void updateUserAccountInfo(UserAccountInfo userAccountInfo) {

        long accountId = userAccountInfo.getId();
        long userId = userAccountInfo.getUserId();

        String key = RedisKeyUtil.getUserAccountEnvRTDataKey(userAccountInfo.getRunEnv(), userAccountInfo.getTradeType());
        String hashKey = String.valueOf(accountId);

        //只发实时的部分数据
        String value = JSONObject.toJSONString(new AccountRTData(userId, accountId, userAccountInfo.getAccountBalanceInfo(), userAccountInfo.getAccountPositionInfo()));

        log.info("更新账户信息，key[{}], value[{}]", key, value);
//        batchWriteSupporter.writeToRedis(key, value);
        batchWriteSupporter.writeToRedisHash(key, hashKey, value);
    }

    /**
     * 更新UserInfo到Redis，包括User名下的账户信息
     *
     * @param env       运行环境
     * @param tradeType 交易类型
     */
    public void updateUserInfoToRedis(RunEnv env, TradeType tradeType) {
        List<UserInfo> userInfos = queryAll();
        for (UserInfo userInfo : userInfos) {

            String key = RedisKeyUtil.getUserInfoKeyPrefix(env, tradeType) + userInfo.getId();
            batchWriteSupporter.writeToRedis(key, JSONObject.toJSONString(userInfo));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (KeyValue<RunEnv, TradeType> keyValue : realtimeConfig.getRun_type().getRunTypeList()) {
            updateUserInfoToRedis(keyValue.getKey(), keyValue.getValue());
        }
    }
}
