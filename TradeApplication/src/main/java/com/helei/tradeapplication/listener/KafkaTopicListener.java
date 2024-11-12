package com.helei.tradeapplication.listener;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


@Slf4j
public abstract class KafkaTopicListener<R> implements MessageListener<String, String> {

    private final ExecutorService executor;

    protected KafkaTopicListener(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void onMessage(@NotNull ConsumerRecord<String, String> record) {
        executor.execute(()->{
            String topic = record.topic();
            String value = record.value();
            log.info("topic[{}]收到消息[{}]", topic, value);

            if (StrUtil.isBlank(value)) {
                log.warn("receive null kafka trade signal, topic[{}] key [{}]", topic, record.key());
                return;
            }

            try {
                invoke(topic, convertJsonToTarget(value));
            } catch (Exception e) {
                log.error("处理kafka topic[{}] 消息[{}]时出错", topic, value, e);
            }
        });
    }

    @Override
    public void onMessage(@NotNull ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        executor.execute(()->{
            String topic = record.topic();
            String value = record.value();
            log.info("topic[{}]收到消息[{}]", topic, value);
            if (StrUtil.isBlank(value)) {
                log.warn("receive null kafka trade signal, topic[{}] key [{}]", topic, record.key());
                return;
            }

            try {
                R r = convertJsonToTarget(value);

                CompletableFuture<Boolean> invoke = invoke(topic, r);
                if (invoke != null) {
                    invoke.thenAcceptAsync(success->{
                        if (success) acknowledgment.acknowledge();
                    }, executor);
                }
            } catch (Exception e) {
                log.error("处理kafka topic[{}] 消息[{}]时出错", topic, value, e);
            }
        });
    }


    public abstract R convertJsonToTarget(String json);


    public abstract CompletableFuture<Boolean> invoke(String topic, R message);
}
