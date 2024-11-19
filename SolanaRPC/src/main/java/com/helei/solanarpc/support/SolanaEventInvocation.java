package com.helei.solanarpc.support;

import com.alibaba.fastjson.JSONObject;
import com.helei.solanarpc.constants.SolanaWSEventType;
import com.helei.solanarpc.dto.SolanaWSRequestContext;

public interface SolanaEventInvocation {


    /**
     * 执行solana websocket 订阅的事件
     *
     * @param eventType    eventType
     * @param context      context
     * @param event        event
     */
    void invoke(SolanaWSEventType eventType, SolanaWSRequestContext context, JSONObject event);

}
