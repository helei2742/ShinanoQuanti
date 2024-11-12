package com.helei.tradeapplication.service;

import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.trade.TradeSignal;

import java.util.concurrent.CompletableFuture;

public interface TradeSignalService {


    /**
     * 处理交易信号
     *
     * @param runEnv    运行环境
     * @param tradeType 交易类型
     * @param signal    信号
     * @return 是否执行成功
     */
    CompletableFuture<Boolean> resolveTradeSignal(RunEnv runEnv, TradeType tradeType, TradeSignal signal);
}
