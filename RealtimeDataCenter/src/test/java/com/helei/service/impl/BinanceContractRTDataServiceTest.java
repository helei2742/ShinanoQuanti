package com.helei.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BinanceContractRTDataServiceTest {

    @Autowired
    private BinanceContractRTDataService binanceContractRTDataService;


    @Autowired
    private

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void startSyncRealTimeKLine() throws InterruptedException {
        binanceContractRTDataService.startSyncRealTimeKLine();

        TimeUnit.MINUTES.sleep(1000);
    }
}
