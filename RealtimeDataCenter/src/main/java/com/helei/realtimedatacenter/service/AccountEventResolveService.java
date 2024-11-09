package com.helei.realtimedatacenter.service;

import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserAccountRealTimeInfo;

public interface AccountEventResolveService {


    /**
     * 处理账户事件
     *
     * @param accountInfo  账户信息
     * @param accountEvent 账户事件
     */
    void resolveAccountEvent(UserAccountInfo accountInfo, AccountEvent accountEvent);
}
