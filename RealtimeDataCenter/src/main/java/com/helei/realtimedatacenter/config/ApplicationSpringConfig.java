package com.helei.realtimedatacenter.config;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.realtimedatacenter.manager.ExecutorServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class ApplicationSpringConfig {


    private final RealtimeConfig realtimeConfig = RealtimeConfig.INSTANCE;


    @Autowired
    private ExecutorServiceManager executorServiceManager;


    @Bean
    public BinanceBaseClientManager binanceBaseWSClientManager() {
        return CEXApiFactory.binanceBaseWSClientManager(realtimeConfig.getRun_type(), executorServiceManager.getConnectExecutor());
    }

    @Bean
    public Map<String, Object> kafkaConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, realtimeConfig.getKafka().getBootstrap_servers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, realtimeConfig.getKafka().getGroup_id());  // 消费者组ID
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return configProps;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        Map<String, Object> configs = kafkaConfigs();

        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }


    @Bean(name = "kafkaAdminClient")
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaConfigs());
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(realtimeConfig.getRedis().getUrl());
        return Redisson.create(config);
    }
}

