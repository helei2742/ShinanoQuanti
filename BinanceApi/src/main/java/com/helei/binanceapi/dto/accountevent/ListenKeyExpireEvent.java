package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * listenKey过期事件
 * <p>当前连接使用的有效listenKey过期时，user data stream 将会推送此事件。</p>
 * <p>注意:</p>
 * <p>
 * 1.此事件与websocket连接中断没有必然联系
 * 2.只有正在连接中的有效listenKey过期时才会收到此消息
 * 3.收到此消息后user data stream将不再更新，直到用户使用新的有效的listenKey
 * </p>
 */
@Getter
@Setter
@ToString

public class ListenKeyExpireEvent extends AccountEvent {

    public ListenKeyExpireEvent(Long eventTime) {
        super(AccountEventType.LISTEN_KEY_EXPIRED, eventTime);
    }

}
