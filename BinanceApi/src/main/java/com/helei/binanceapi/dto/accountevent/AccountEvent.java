package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AccountEvent {

    /**
     * 事件类型
     */
    private final AccountEventType eventType;

    /**
     * 事件时间
     */
    private Long eventTime;

    public AccountEvent(AccountEventType eventType) {
        this.eventType = eventType;
    }
}
