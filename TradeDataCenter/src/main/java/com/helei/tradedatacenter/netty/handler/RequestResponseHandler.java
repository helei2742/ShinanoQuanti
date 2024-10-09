package com.helei.tradedatacenter.netty.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


@Slf4j
public class RequestResponseHandler<T> {

    private final ConcurrentMap<String, HandlerEntity<T>> requestIdMap = new ConcurrentHashMap<>();

    private final long liveSecond = 300;

    /**
     * 注册request
     * @param id request的id
     * @return 是否注册成功
     */
    public boolean registryRequest(String id, Consumer<T> callback) {
        AtomicBoolean res = new AtomicBoolean(false);
        requestIdMap.compute(id, (k, v)->{
            if (v == null) {
                res.set(true);
                v = new HandlerEntity<>(System.currentTimeMillis() + liveSecond * 1000, callback);
                log.debug("registry request id[{}] success", id);
            }
            return v;
        });

        return res.get();
    }

    /**
     * 提交resoonse
     * @param id id
     * @param response response
     */
    public boolean submitResponse(String id, T response) {
        HandlerEntity<T> entity = requestIdMap.get(id);
        if (entity == null) {
            log.warn("request id[{}} didn't exist", id);
            return false;
        } else {
            if (entity.expireTime > System.currentTimeMillis()) {
                log.warn("request id[{}] expired, cancel invoke callback", id);
                return false;
            } else {
                entity.callback.accept(response);
                log.debug("invoke request id[{}] callback success", id);
                return false;
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    static class HandlerEntity<T>{
        private long expireTime;

        private Consumer<T> callback;
    }
}
