package com.helei.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.reaktimedatacenter.service.impl.BinanceMarketRTDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class BinanceMarketRTDataServiceTest {

    @Autowired
    private BinanceMarketRTDataService binanceMarketRTDataService;



    @Test
    void startSyncRealTimeKLine() throws InterruptedException {
        binanceMarketRTDataService.startSyncRealTimeKLine(RunEnv.TEST_NET, TradeType.SPOT);

        TimeUnit.MINUTES.sleep(1000);
    }
}
