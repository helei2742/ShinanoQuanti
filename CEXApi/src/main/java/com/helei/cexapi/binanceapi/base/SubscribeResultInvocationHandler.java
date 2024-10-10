package com.helei.cexapi.binanceapi.base;


import com.alibaba.fastjson.JSONObject;


/**
 * 订阅流消息结果处理
 */
public interface SubscribeResultInvocationHandler {

    void invoke(String streamName, JSONObject result);

}
