package com.helei.binanceapi.base;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.netty.base.AbstractWebSocketClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 能够处理推送的消息，只要消息体的 e 字段为已注册的名
 */
@Slf4j
public abstract class AbstractBinanceWSApiClientHandler extends AbstractWebSocketClientHandler<JSONObject, JSONObject> {
    /**
     * 存储订阅流的信息，包含有收到流消息的回调。
     */
    protected final ConcurrentMap<String, StreamSubscribeEntity> subscribeMap;


    public AbstractBinanceWSApiClientHandler() {
        this.subscribeMap = new ConcurrentHashMap<>();
    }

    @Override
    public JSONObject convertMessageToRespType(String message) {
        return JSONObject.parseObject(message);
    }

    @Override
    public String getRequestId(JSONObject request) {
        return request.getString("id");
    }

    @Override
    public String getResponseId(JSONObject response) {
        return response.getString("id");
    }

    @Override
    protected void handleOtherMessage(JSONObject message) {
        String streamName = message.getString("e");

        if (!StrUtil.isBlank(streamName)) {
            handleStreamMessage(streamName, message);
        }
    }




    /**
     * 处理stream流推送消息
     *
     * @param streamName stream 流名称
     * @param content    推送消息
     */
    protected abstract void handleStreamMessage(String streamName, JSONObject content);

    /**
     * 创建订阅
     *
     * @param subList stream订阅类型列表
     */
    public void addSubscribe(List<StreamSubscribeEntity> subList) {
        for (StreamSubscribeEntity subscribeEntity : subList) {
            subscribeMap.putIfAbsent(subscribeEntity.getStreamName(), subscribeEntity);
        }
    }

}

