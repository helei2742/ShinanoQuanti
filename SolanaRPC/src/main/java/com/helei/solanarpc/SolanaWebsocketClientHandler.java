
package com.helei.solanarpc;

import com.alibaba.fastjson.JSONObject;
import com.helei.netty.base.AbstractWebSocketClientHandler;
import com.helei.solanarpc.constants.SolanaWSEventType;
import com.helei.solanarpc.dto.SolanaWSRequestContext;
import com.helei.solanarpc.support.SolanaEventInvocation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
public class SolanaWebsocketClientHandler extends AbstractWebSocketClientHandler<JSONObject, JSONObject> {

    /**
     * 订阅id map accountAddress
     */
    private final ConcurrentHashMap<Long, SolanaWSRequestContext> subscribeIdMapContext = new ConcurrentHashMap<>();


    @Override
    protected void handleOtherMessage(JSONObject message) {

        if (message.containsKey("method")) {
            SolanaWSEventType solanaWSEventType = SolanaWSEventType.valueOf(message.getString("method"));
            log.debug("收到事件[{}]推送消息[{}]", solanaWSEventType, message);

            Long subscription = message.getJSONObject("params").getLong("subscription");
            SolanaWSRequestContext context = subscribeIdMapContext.get(subscription);

            if (context == null) {
                log.warn("订阅id[{}]没有对应的账户地址信息, 将不会处理事件[{}]", subscription, message);
                return;
            }

            SolanaEventInvocation invocation = context.getInvocation();

            CompletableFuture.runAsync(()->{
                invocation.invoke(solanaWSEventType, context, message);
            }).exceptionally(throwable -> {
                log.error("处理事件[{}]发生错误, context[{}], subscription[{}], message[{}]", solanaWSEventType, context, subscription, message, throwable);
                return null;
            });
        } else {
            log.warn("收到未知消息[{}]", message);
        }
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
}


