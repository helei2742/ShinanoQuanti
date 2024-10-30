package com.helei.reaktimedatacenter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class ApplicationSpringConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean(name = "kafkaAdminClient")
    public AdminClient kafkaAdminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", bootstrapServers);  // 确保这里是 bootstrap.servers
        return AdminClient.create(configs);
    }

}
