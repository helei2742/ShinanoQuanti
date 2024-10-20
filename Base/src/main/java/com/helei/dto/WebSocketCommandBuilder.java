package com.helei.dto;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.helei.constants.WSCommandType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * WebSocket里发送请求的格式
 */
@Data
@EqualsAndHashCode
public class WebSocketCommandBuilder {
    private final JSONObject command;

    WebSocketCommandBuilder() {
        command = new JSONObject();
        command.put("id", UUID.randomUUID().toString());
        command.put("params", new JSONObject());
    }

    public static WebSocketCommandBuilder builder() {
        return new WebSocketCommandBuilder();
    }


    public WebSocketCommandBuilder setCommandType(WSCommandType wsCommandType) {
        command.put("method", wsCommandType.getDescription());
        return this;
    }

    public WebSocketCommandBuilder setParams(JSONObject param) {
        command.put("params", param);
        return this;
    }

    public JSONObject build() {
        return command;
    }

    /**
     * 添加kv类型参数，
     *
     * @param key   key
     * @param value value
     * @return WebSocketCommandBuilder
     */
    public WebSocketCommandBuilder addParam(String key, Object value) {
        synchronized (command) {
            if (!command.containsKey("params")) {
                command.put("params", new JSONObject());
            }
            command.getJSONObject("params").put(key, value);
        }
        return this;
    }

    /**
     * 添加array类型参数，
     *
     * @param value value
     * @return WebSocketCommandBuilder
     */
    public WebSocketCommandBuilder addArrayParam(Object value) {
        synchronized (command) {
            if (!command.containsKey("params")) {
                command.put("params", new JSONArray());
            }
            command.getJSONArray("params").add(value);
        }
        return this;
    }

    /**
     * 清除参数
     *
     * @return
     */
    public WebSocketCommandBuilder clear() {
        synchronized (command) {
            command.put("id", UUID.randomUUID().toString());
            command.remove("params");
        }
        return this;
    }
}

