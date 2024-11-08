package com.helei.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.realtimedatacenter.service.impl.market.BinanceMarketRTDataService;
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
