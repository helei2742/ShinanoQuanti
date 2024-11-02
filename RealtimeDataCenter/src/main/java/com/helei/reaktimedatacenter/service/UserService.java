package com.helei.reaktimedatacenter.service;

import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 查询所有user
     * @return UserInfo list
     */
    List<UserInfo> queryAll();


    /**
     * 更新用户账户信息
     * @param userAccountInfo userAccountInfo
     */
    void updateUserAccountInfo(UserAccountInfo userAccountInfo);
}
