package com.helei.service.impl;

import com.helei.reaktimedatacenter.service.impl.BinanceContractRTDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class BinanceContractRTDataServiceTest {

    @Autowired
    private BinanceContractRTDataService binanceContractRTDataService;



    @Test
    void startSyncRealTimeKLine() throws InterruptedException {
        binanceContractRTDataService.startSyncRealTimeKLine();

        TimeUnit.MINUTES.sleep(1000);
    }
}
