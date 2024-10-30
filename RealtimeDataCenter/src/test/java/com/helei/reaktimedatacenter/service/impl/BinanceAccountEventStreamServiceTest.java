package com.helei.reaktimedatacenter.service.impl;

import com.helei.dto.account.UserInfo;
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

        binanceAccountEventStreamService.startUserInfoEventStream(first);

        TimeUnit.MINUTES.sleep(1000);
    }
}
