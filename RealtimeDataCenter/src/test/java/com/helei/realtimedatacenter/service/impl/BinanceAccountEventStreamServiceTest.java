package com.helei.realtimedatacenter.service.impl;

import com.helei.dto.account.UserInfo;
import com.helei.realtimedatacenter.service.impl.account.BinanceAccountEventStreamService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class BinanceAccountEventStreamServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private BinanceAccountEventStreamService binanceAccountEventStreamService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void startAllUserInfoEventStream() {
        binanceAccountEventStreamService.startAllUserInfoEventStream();

    }

    @Test
    void startUserInfoEventStream() throws InterruptedException {

        List<UserInfo> userInfos = userService.queryAll();

        UserInfo first = userInfos.getFirst();
        UserInfo two = userInfos.get(1);
        UserInfo three = userInfos.get(2);

        binanceAccountEventStreamService.startUserInfoEventStream(three);

        TimeUnit.MINUTES.sleep(1000);
    }
}
