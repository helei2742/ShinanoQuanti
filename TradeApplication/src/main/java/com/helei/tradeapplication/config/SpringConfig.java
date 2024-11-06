package com.helei.tradeapplication.config;


import com.helei.dto.config.SnowFlowConfig;
import com.helei.snowflack.SnowFlakeFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class SpringConfig {


    private final TradeAppConfig tradeAppConfig = TradeAppConfig.INSTANCE;

    @Bean
    public Map<String, Object> kafkaConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, tradeAppConfig.getKafka().getBootstrap_servers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, tradeAppConfig.getKafka().getGroup_id());  // 消费者组ID
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return configProps;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        return new DefaultKafkaConsumerFactory<>(kafkaConfigs());
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
        config.useSingleServer().setAddress(tradeAppConfig.getRedis().getUrl());
        return Redisson.create(config);
    }


    @Bean
    public SnowFlakeFactory snowFlakeFactory() {
        SnowFlowConfig snowFlow = tradeAppConfig.getRun_type().getSnow_flow();
        return new SnowFlakeFactory(snowFlow.getDatacenter_id(), snowFlow.getMachine_id());
    }
}
