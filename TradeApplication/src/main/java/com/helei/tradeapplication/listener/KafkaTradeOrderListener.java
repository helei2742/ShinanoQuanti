package com.helei.tradeapplication.listener;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.tradeapplication.dto.TradeOrderGroup;
import com.helei.tradeapplication.service.TradeOrderCommitService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class KafkaTradeOrderListener extends KafkaTopicListener<TradeOrderGroup>{

    /**
     * 运行环境
     */
    private final RunEnv runEnv;

    /**
     * 交易类型
     */
    private final TradeType tradeType;


    /**
     * 提交订单服务
     */
    private final TradeOrderCommitService tradeOrderCommitService;


    public KafkaTradeOrderListener(RunEnv runEnv, TradeType tradeType, TradeOrderCommitService tradeOrderCommitService, ExecutorService executor) {
        super(executor);
        this.runEnv = runEnv;
        this.tradeType = tradeType;
        this.tradeOrderCommitService = tradeOrderCommitService;
    }

    @Override
    public TradeOrderGroup convertJsonToTarget(String json) {
        return JSONObject.parseObject(json, TradeOrderGroup.class);
    }

    @Override
    public CompletableFuture<Boolean> invoke(String topic, TradeOrderGroup orderGroup) {
        log.info("topic[{}]收到交易订单,runEnv[{}]-tradeType[{}]-orderGroup[{}]", topic, runEnv, tradeType, orderGroup);

        try {
            return tradeOrderCommitService.traderOrderHandler(runEnv, tradeType, orderGroup);
        } catch (Exception e) {
            log.error("处理topic[{}}信号发生错误", topic, e);
            return null;
        }
    }

}
