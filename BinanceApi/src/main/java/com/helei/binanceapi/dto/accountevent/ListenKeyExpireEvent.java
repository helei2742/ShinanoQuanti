package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * listenKey过期事件
 */
@Getter
@Setter
public class ListenKeyExpireEvent extends AccountEvent {

    public ListenKeyExpireEvent(Long eventTime) {
        super(AccountEventType.LISTEN_KEY_EXPIRED, eventTime);
    }

}
