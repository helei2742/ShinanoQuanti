package com.helei.telegramebot.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.dto.base.KeyValue;
import com.helei.telegramebot.config.TwitterConfig;
import com.helei.telegramebot.manager.ExecutorServiceManager;
import com.helei.telegramebot.service.ITwitterMessageService;
import com.helei.twiterstream.TwitterStreamRuleClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
//@Service
@Deprecated
public class TwitterMessageServiceImpl implements ITwitterMessageService {

    private final TwitterConfig twitterConfig = TwitterConfig.INSTANCE;

    private final ExecutorService executor;

    private final TwitterStreamRuleClient twitterStreamRuleClient;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final List<Consumer<JSONObject>> eventListeners = new ArrayList<>();

    public TwitterMessageServiceImpl(ExecutorServiceManager manager) throws MalformedURLException {
        executor = manager.getCommonExecutor();
        TwitterConfig.TwitterUrlConfig urlConfig = twitterConfig.getUrl();
        this.twitterStreamRuleClient = new TwitterStreamRuleClient(
                urlConfig.getRule_url(),
                urlConfig.getStream_url(),
                twitterConfig.getBearer_token(),
                executor
        );
    }


    @Override
    public ITwitterMessageService initConfigFileRule() {
        List<KeyValue<String, String>> filterRule = twitterConfig.getFilter_rule();
        for (KeyValue<String, String> keyValue : filterRule) {
            twitterStreamRuleClient.addFilterRule(keyValue.getKey(), keyValue.getValue());
        }

        return this;
    }

    @Override
    public void startListenStream() {
        twitterStreamRuleClient
                .listenToStream()
                .thenAcceptAsync(buffer->{
                    running.set(true);
                    try {
                        JSONObject event = null;
                        while (running.get()) {
                            event = buffer.take();
                            JSONObject finalEvent = event;
                            for (Consumer<JSONObject> eventListener : eventListeners) {
                                CompletableFuture.runAsync(()->{
                                    eventListener.accept(finalEvent);
                                }, executor);
                            }
                        }

                    } catch (InterruptedException e) {
                        log.error("从缓存中获取twitter事件失败", e);
                        closeListenStream();
                    }
                });
    }



    @Override
    public void closeListenStream() {
        twitterStreamRuleClient.close();
    }
}

