package com.helei.realtimedatacenter.service.impl;


import com.helei.dto.account.UserAccountInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RealtimeAccountInfoSpace {

    private final ConcurrentMap<Long, UserAccountInfo> userAccountInfoMap = new ConcurrentHashMap<>();


}
