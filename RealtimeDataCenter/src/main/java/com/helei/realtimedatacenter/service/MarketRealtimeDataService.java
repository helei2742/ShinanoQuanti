package com.helei.realtimedatacenter.service;


import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;

import java.util.Set;

public interface MarketRealtimeDataService {


    /**
     * 同步实时k线
     *
     * @return k线种类
     */
    Set<String> startSyncRealTimeKLine();

    /**
     * 开始同步实时k线
     * @param runEnv runEnv
     * @param tradeType tradeType
     * @return k线种数
     */
    Set<String> startSyncEnvRealTimeKLine(RunEnv runEnv, TradeType tradeType);

}
