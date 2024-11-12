package com.helei.tradeapplication.service;


import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.tradeapplication.dto.TradeOrderGroup;

import java.util.concurrent.CompletableFuture;

/**
 * 负责提交订单
 */
public interface TradeOrderCommitService {


    /**
     * 处理交易信号
     *
     * @param runEnv     运行环境
     * @param tradeType  交易类型
     * @param orderGroup 订单
     * @return 是否全部提交
     */
    CompletableFuture<Boolean> traderOrderHandler(RunEnv runEnv, TradeType tradeType, TradeOrderGroup orderGroup);
}
