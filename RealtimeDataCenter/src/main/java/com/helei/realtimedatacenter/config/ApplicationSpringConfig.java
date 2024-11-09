package com.helei.realtimedatacenter.config;

import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.realtimedatacenter.manager.ExecutorServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

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
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, realtimeConfig.getKafka().getBootstrap_servers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");  // 可选设置
        return configProps;
    }
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configs = kafkaConfigs();
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
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

