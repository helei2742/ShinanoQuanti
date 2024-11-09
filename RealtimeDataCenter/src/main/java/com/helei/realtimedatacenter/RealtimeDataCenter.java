package com.helei.realtimedatacenter;

import com.helei.realtimedatacenter.service.AccountEventStreamService;
import com.helei.realtimedatacenter.service.MarketRealtimeDataService;
import com.helei.realtimedatacenter.service.UserService;
import com.helei.realtimedatacenter.service.impl.market.RandomMarketRTDataService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 实时数据中心
 */
@SpringBootApplication
public class RealtimeDataCenter {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RealtimeDataCenter.class, args);


        startRTDataStream(applicationContext);

//        updateAllUserInfo2Redis(applicationContext);
//
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
        MarketRealtimeDataService marketRealtimeDataService = applicationContext.getBean(RandomMarketRTDataService.class);
        marketRealtimeDataService.startSyncRealTimeKLine();
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
