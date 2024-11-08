package com.helei.binanceapi.base;


import com.alibaba.fastjson.JSONObject;

import java.util.Map;


/**
 * 订阅流消息结果处理
 */
public interface SubscribeResultInvocationHandler {

    void invoke(String streamName, Map<String, Object> params, JSONObject result);

}
