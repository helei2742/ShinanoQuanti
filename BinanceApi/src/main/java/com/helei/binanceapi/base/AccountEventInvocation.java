package com.helei.binanceapi.base;

import com.helei.binanceapi.dto.accountevent.AccountEvent;

public interface AccountEventInvocation {

    void whenReceiveEvent(AccountEvent event);

    String lengthListenKey(String oldListenKey);
}
