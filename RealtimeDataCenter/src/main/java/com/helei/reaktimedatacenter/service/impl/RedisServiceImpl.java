package com.helei.reaktimedatacenter.service.impl;

import com.helei.reaktimedatacenter.service.RedisService;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private static final String REDIS_LOCK_PREFIX = "REDIS_LOCK:";


    @Autowired
    private RedissonClient redissonClient;


    /**
     * 写入key value值
     * @param key key
     * @param value value
     */
    public void saveKeyValue(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);  // 写入 Redis
    }



    public void safeInvoke(
            String key,
            long releaseTime,
            TimeUnit timeUnit,
            Runnable accountUpdateTask
    ) {
        RLock lock = redissonClient.getLock(REDIS_LOCK_PREFIX + key);

        try {
            lock.lock(releaseTime, timeUnit);
            accountUpdateTask.run();
        } finally {
            lock.unlock();
        }
    }

}
