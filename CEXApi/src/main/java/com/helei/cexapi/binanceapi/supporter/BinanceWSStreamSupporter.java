package com.helei.cexapi.binanceapi.supporter;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;



@Slf4j
public class BinanceWSStreamSupporter {

    private final ConcurrentMap<String, StreamSubscribeEntity> subscribeMap;

    public BinanceWSStreamSupporter() {
        this.subscribeMap = new ConcurrentHashMap<>();
    }


    /**
     * 创建订阅
     * @param symbol symbol
     * @param subList  stream订阅类型列表
     */
    public void addSubscribe(String symbol, List<StreamSubscribeEntity> subList) {
        for (StreamSubscribeEntity subscribeEntity : subList) {
            subscribeMap.putIfAbsent(subscribeEntity.getStreamName(), subscribeEntity);
        }
    }

    /**
     * 收到订阅的消息，执行相应的回调
     * 1.首先会优先使用 StreamSubscribeEntity中传入的线程池执行
     * 2.如果没有则会用AbstractBinanceWSApiClient中的线程池执行
     * 3.如果还没有，使用netty线程池执行
     * @param streamName          streamName
     * @param message         message
     * @param callbackInvoker callbackInvoker
     */
    public void publishStreamResponse(String streamName, JSONObject message, ExecutorService callbackInvoker) {

        StreamSubscribeEntity subscribeEntity = subscribeMap.get(streamName);
        if (subscribeEntity == null) {
            log.error("No subscribe entity for stream type {}", streamName);
            return;
        }

        try {
            ExecutorService executor = subscribeEntity.getCallbackExecutor();
            if (executor != null) {
                executor.submit(()->{
                    subscribeEntity.getInvocationHandler().invoke(streamName, message);
                });
            } else if (callbackInvoker != null){
                callbackInvoker.submit(()->{
                    subscribeEntity.getInvocationHandler().invoke(streamName, message);
                });
            } else {
                subscribeEntity.getInvocationHandler().invoke(streamName, message);
                log.warn("use netty thread pool execute, stream name [{}]", streamName);
            }
        } catch (Exception e) {
            log.error("publish stream response error, stream name[{}]", streamName, e);
        }
    }
}

