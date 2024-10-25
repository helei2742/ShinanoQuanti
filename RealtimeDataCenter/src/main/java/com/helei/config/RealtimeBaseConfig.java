package com.helei.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shinano.quantity.realtime.base")
public class RealtimeBaseConfig {

    private Integer kafkaKlineNumPartitions;

    private Short kafkaKlineReplicationFactor;
}
