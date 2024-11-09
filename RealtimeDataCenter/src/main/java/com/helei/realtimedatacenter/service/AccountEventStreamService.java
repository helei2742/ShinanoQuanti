package com.helei.realtimedatacenter.service;

import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserAccountRealTimeInfo;
import com.helei.dto.account.UserInfo;

public interface AccountEventStreamService {

    /**
     * 开启所有用户事件流
     */
    void startAllUserInfoEventStream();

    /**
     * 开启用户信息事件，会开启用户名下所有账户的事件流
     * @param userInfo userInfo
     */
    void startUserInfoEventStream(UserInfo userInfo);

    /**
     * 解析账户事件
     * @param accountInfo  accountInfo
     * @param accountEvent accountEvent
     */
    void resolveAccountEvent(final UserAccountInfo accountInfo, AccountEvent accountEvent);
}
