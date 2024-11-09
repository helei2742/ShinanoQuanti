package com.helei.tradeapplication;


import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.KeyValue;
import com.helei.tradeapplication.cache.UserInfoCache;
import com.helei.tradeapplication.config.TradeAppConfig;
import com.helei.tradeapplication.service.impl.KafkaConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootApplication
@MapperScan("com.helei.tradeapplication.mapper")
public class TradeApplication {

    private static TradeAppConfig tradeAppConfig = TradeAppConfig.INSTANCE;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TradeApplication.class, args);


//        startTradeSignalConsumer(applicationContext);

        initAllUserInfo(applicationContext);
    }

    /**
     * 开启交易信号消费
     *
     * @param applicationContext applicationContext
     */
    private static void startTradeSignalConsumer(ConfigurableApplicationContext applicationContext) {
        KafkaConsumerService kafkaConsumerService = applicationContext.getBean(KafkaConsumerService.class);
        for (KeyValue<RunEnv, TradeType> keyValue : tradeAppConfig.getRun_type().getRunTypeList()) {
            kafkaConsumerService.startTradeSignalConsumer(keyValue.getKey(), keyValue.getValue());
        }
    }


    /**
     * 初始化用户信息
     *
     * @param applicationContext applicationContext
     */
    private static void initAllUserInfo(ConfigurableApplicationContext applicationContext) {
        UserInfoCache userInfoCache = applicationContext.getBean(UserInfoCache.class);
        try {
            userInfoCache.updateUserBaseAndRTInfo();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
