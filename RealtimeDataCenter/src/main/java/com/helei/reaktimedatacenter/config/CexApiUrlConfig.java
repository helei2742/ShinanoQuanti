package com.helei.reaktimedatacenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shinano.quantity.url.binance-api-url")
public class CexApiUrlConfig {

    private String uContractStreamUrl;
}
