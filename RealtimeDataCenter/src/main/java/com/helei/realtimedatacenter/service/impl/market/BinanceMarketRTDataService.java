package com.helei.realtimedatacenter.service.impl.market;

import cn.hutool.core.lang.Pair;
import com.helei.binanceapi.BinanceWSMarketStreamClient;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.realtimedatacenter.manager.ExecutorServiceManager;
import com.helei.realtimedatacenter.realtime.impl.BinanceKLineRTDataSyncTask;
import com.helei.realtimedatacenter.service.impl.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


/**
 * 币安市场数据服务
 */
@Slf4j
@Service
public class BinanceMarketRTDataService extends AbstractKafkaMarketRTDataService {

    @Autowired
    private BinanceBaseClientManager binanceBaseClientManager;


    @Autowired
    public BinanceMarketRTDataService(ExecutorServiceManager executorServiceManager, KafkaProducerService kafkaProducerService) {
        super(executorServiceManager.getKlineTaskExecutor(), kafkaProducerService);
    }

    @Override
    protected CompletableFuture<Void> registryKLineDataLoader(
            RunEnv runEnv,
            TradeType tradeType,
            List<Pair<String, KLineInterval>> listenKLines,
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService executorService
    ) throws ExecutionException, InterruptedException {
        AbstractBinanceWSApiClient client = binanceBaseClientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.MARKET_STREAM).get();
        BinanceWSMarketStreamClient marketStreamClient = (BinanceWSMarketStreamClient) client;

        return new BinanceKLineRTDataSyncTask(
                marketStreamClient,
                listenKLines
        ).startSync(whenReceiveKLineData, taskExecutor);
    }
}

