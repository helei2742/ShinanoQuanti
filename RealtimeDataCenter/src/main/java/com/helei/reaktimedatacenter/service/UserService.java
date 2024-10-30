package com.helei.reaktimedatacenter.service;

import com.helei.dto.account.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 查询所有user
     * @return UserInfo list
     */
    List<UserInfo> queryAll();
}
