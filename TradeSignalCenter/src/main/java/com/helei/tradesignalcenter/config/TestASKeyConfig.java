package com.helei.tradesignalcenter.config;

import com.helei.dto.ASKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TestASKeyConfig {

    @Bean
    public ASKey contractTestnetASKey(@Value("${asKey.test.contract.apiKey}") String ak, @Value("${asKey.test.contract.secretKey}") String sk) {
        return new ASKey(ak, sk);
    }

    @Bean
    public ASKey spotTestnetASKey(@Value("${asKey.test.spot.apiKey}") String ak, @Value("${asKey.test.spot.secretKey}") String sk) {
        return new ASKey(ak, sk);
    }
}
