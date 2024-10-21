package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * listenKey过期事件
 */
@Getter
@Setter
@ToString

public class ListenKeyExpireEvent extends AccountEvent {

    public ListenKeyExpireEvent(Long eventTime) {
        super(AccountEventType.LISTEN_KEY_EXPIRED, eventTime);
    }

}
