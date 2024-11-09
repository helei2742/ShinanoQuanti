package com.helei.realtimedatacenter.service;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.UserAccountInfo;
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
     * 更新用户账户信息
     *
     * @param userAccountInfo userAccountInfo
     */
    void updateUserAccountInfo(UserAccountInfo userAccountInfo);

    /**
     * 更新所有用户信息
     */
    void updateAllUserInfo();
}
