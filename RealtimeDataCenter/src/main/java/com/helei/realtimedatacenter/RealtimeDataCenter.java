package com.helei.realtimedatacenter;

import com.helei.binanceapi.BinanceWSMarketStreamClient;
import com.helei.realtimedatacenter.service.AccountEventStreamService;
import com.helei.realtimedatacenter.service.MarketRealtimeDataService;
import com.helei.realtimedatacenter.service.UserService;
import com.helei.realtimedatacenter.service.impl.market.BinanceMarketRTDataService;
import com.helei.realtimedatacenter.service.impl.market.RandomMarketRTDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

/**
 * 实时数据中心
 */
@SpringBootApplication
public class RealtimeDataCenter {
    private static final Logger log = LoggerFactory.getLogger(RealtimeDataCenter.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RealtimeDataCenter.class, args);


        startRTDataStream(applicationContext);

//        updateAllUserInfo2Redis(applicationContext);
//
//        startAccountEventStream(applicationContext);
    }

    /**
     * 开启账户事件流监听
     *
     * @param applicationContext applicationContext
     */
    private static void startAccountEventStream(ConfigurableApplicationContext applicationContext) {
        AccountEventStreamService accountEventStreamService = applicationContext.getBean(AccountEventStreamService.class);
        accountEventStreamService.startAllUserInfoEventStream();
    }

    /**
     * 开启实时数据流
     *
     * @param applicationContext applicationContext
     */
    private static void startRTDataStream(ConfigurableApplicationContext applicationContext) {
        MarketRealtimeDataService marketRealtimeDataService = applicationContext.getBean(BinanceMarketRTDataService.class);

        Set<String> set = marketRealtimeDataService.startSyncRealTimeKLine();
        log.info("开始同步实时k线: {}", set);
    }

    /**
     * 更新用户信息到redis
     *
     * @param applicationContext applicationContext
     */
    private static void updateAllUserInfo2Redis(ConfigurableApplicationContext applicationContext) {
        UserService userService = applicationContext.getBean(UserService.class);
        userService.updateAllUserInfo();
    }
}

