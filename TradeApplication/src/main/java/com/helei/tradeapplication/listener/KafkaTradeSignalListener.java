package com.helei.tradeapplication.listener;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.trade.TradeSignal;
import com.helei.tradeapplication.service.TradeSignalService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


/**
 * kafka交易信号监听器
 */
@Slf4j
public class KafkaTradeSignalListener extends KafkaTopicListener<TradeSignal> {

    /**
     * 运行环境
     */
    private final RunEnv runEnv;

    /**
     * 交易类型
     */
    private final TradeType tradeType;


    private final TradeSignalService tradeSignalService;

    public KafkaTradeSignalListener(
            RunEnv env,
            TradeType tradeType,
            TradeSignalService tradeSignalService,
            ExecutorService executor
    ) {
        super(executor);

        this.runEnv = env;
        this.tradeType = tradeType;
        this.tradeSignalService = tradeSignalService;
    }

    @Override
    public TradeSignal convertJsonToTarget(String json) {
        return JSONObject.parseObject(json, TradeSignal.class);
    }

    @Override
    public CompletableFuture<Boolean> invoke(String topic, TradeSignal signal) {
        log.info("topic[{}]收到信号,runEnv[{}]-tradeType[{}]-signal[{}]", topic, runEnv, tradeType, signal);

        try {
            return tradeSignalService.resolveTradeSignal(runEnv, tradeType, signal);
        } catch (Exception e) {
            log.error("处理topic[{}}信号发生错误", topic, e);
            return null;
        }
    }

}

