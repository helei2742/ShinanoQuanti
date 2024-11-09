package com.helei.realtimedatacenter.service;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.UserAccountRealTimeInfo;
import com.helei.dto.account.UserAccountStaticInfo;
import com.helei.dto.account.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 查询所有user
     *
     * @return UserInfo list
     */
    List<UserInfo> queryAll();

    /**
     * 查询指定环境的用户
     *
     * @param runEnv    运行环境
     * @param tradeType 交易类型
     * @return user list
     */
    List<UserInfo> queryEnvUser(RunEnv runEnv, TradeType tradeType);

    /**
     * 更新用户账户实时信息
     *
     * @param tradeType    tradeType
     * @param runEnv       runEnv
     * @param realTimeInfo realTimeInfo
     */
    void updateUserAccountRTInfo(RunEnv runEnv, TradeType tradeType, UserAccountRealTimeInfo realTimeInfo);


    /**
     * 更新用户账户历史信息
     *
     * @param tradeType    tradeType
     * @param runEnv       runEnv
     * @param staticInfo staticInfo
     */
    void updateUserAccountStaticInfo(RunEnv runEnv, TradeType tradeType, UserAccountStaticInfo staticInfo);

    /**
     * 更新所有用户信息
     */
    void updateAllUserInfo();
}
