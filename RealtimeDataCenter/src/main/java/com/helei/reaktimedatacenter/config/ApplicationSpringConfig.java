package com.helei.reaktimedatacenter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class ApplicationSpringConfig {


    private final RealtimeConfig realtimeConfig = RealtimeConfig.INSTANCE;

    @Bean(name = "kafkaAdminClient")
    public AdminClient kafkaAdminClient() {

        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", realtimeConfig.getKafka().getBootstrap_servers());  // 确保这里是 bootstrap.servers
        return AdminClient.create(configs);
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(realtimeConfig.getRedis().getUrl());
        return Redisson.create(config);
    }
}
