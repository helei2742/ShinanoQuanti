package com.helei.tradedatacenter.subscribe.binanceapi.dto;


import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.subscribe.binanceapi.constants.WebSocketCommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * WebSocket里发送请求的格式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WebSocketCommand {

    /**
     * 请求的类型
     */
    private String method;

    /**
     * 参数
     */
    private JSONObject params;

    /**
     * id
     */
    private String id;

    public synchronized void setParams(JSONObject params) {
        this.params = params;
    }

    public synchronized void addParam(String key, Object value) {
        if (params == null) {
            params = new JSONObject();
        }
        params.put(key, value);
    }

    public static WebSocketCommandBuilder builder() {
        return new WebSocketCommandBuilder();
    }

    public static class WebSocketCommandBuilder{

        private final WebSocketCommand command;

        WebSocketCommandBuilder() {
            command = new WebSocketCommand();
            command.setId(UUID.randomUUID().toString());
        }

        public WebSocketCommand buildPing() {
            return setCommandType(WebSocketCommandType.PING).build();
        }

        public WebSocketCommand buildPong() {
            return setCommandType(WebSocketCommandType.PONG).build();
        }

        public WebSocketCommandBuilder setCommandType(WebSocketCommandType webSocketCommandType) {
            command.setMethod(webSocketCommandType.getDescription());
            return this;
        }

        public WebSocketCommandBuilder setParams(JSONObject param) {
            command.setParams(param);
            return this;
        }

        public WebSocketCommandBuilder noRateLimit() {
            command.addParam("returnRateLimits ", true);
            return this;
        }

        public WebSocketCommand build() {
            return command;
        }
    }
}
