package com.helei.reaktimedatacenter.supporter;


import com.helei.dto.base.KeyValue;
import com.helei.reaktimedatacenter.manager.ExecutorServiceManager;
import com.helei.reaktimedatacenter.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class BatchWriteSupporter implements InitializingBean {


    /**
     * redis 批量写入的阀值
     */
    private static final int REDIS_BATCH_SIZE = 5;

    /**
     * redis 批量写入的间隔
     */
    private static final int REDIS_BATCH_INTERVAL = 1000;


    /**
     * 是否在运行
     */
    private volatile boolean isRunning = true;


    /**
     * 缓存redis key - < UpdateCount,value>
     */
    private final ConcurrentHashMap<String, KeyValue<Integer, String>> redisKVMap = new ConcurrentHashMap<>();


    @Autowired
    private RedisService redisService;

    @Autowired
    private ExecutorServiceManager executorServiceManager;


    /**
     * 写入redis
     *
     * @param key   key
     * @param value value
     */
    public void writeToRedis(String key, String value) {
        AtomicBoolean isWrite = new AtomicBoolean(false);

        redisKVMap.compute(key, (k, v) -> {
            if (v == null) {
                v = new KeyValue<>(0, "");
            }
            v.setKey(v.getKey() + 1);
            v.setValue(value);

            if (v.getKey() >= REDIS_BATCH_SIZE) isWrite.set(true);
            return v;
        });

        if (isWrite.get()) {
            batchWriteRedis(key);
        }
    }


    /**
     * 写入redis
     *
     * @param key redisKVMap 的key，也是写入redis的key
     */
    private void batchWriteRedis(String key) {
        KeyValue<Integer, String> remove = redisKVMap.remove(key);
        if (remove != null) {
            redisService.saveKeyValue(key, remove.getValue());
        }
    }

    private void batchWriteTask() {
        try {
            while (isRunning) {
                //1.处理redis的
                for (Map.Entry<String, KeyValue<Integer, String>> entry : redisKVMap.entrySet()) {
                    batchWriteRedis(entry.getKey());
                }

                //睡眠一会
                TimeUnit.MILLISECONDS.sleep(REDIS_BATCH_INTERVAL);
            }
        } catch (Exception e) {
            log.error("运行批量写入任务失败", e);
        }
    }


    /**
     * 关闭
     */
    public void shutdown() {
        isRunning = false;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        executorServiceManager.getSyncTaskExecutor().execute(this::batchWriteTask);
    }
}
