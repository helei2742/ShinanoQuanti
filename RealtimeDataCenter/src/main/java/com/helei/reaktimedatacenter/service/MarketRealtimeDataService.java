package com.helei.reaktimedatacenter.service;


import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;

public interface MarketRealtimeDataService {


    Integer startSyncRealTimeKLine();

    /**
     * 开始同步实时k线
     * @param runEnv runEnv
     * @param tradeType tradeType
     * @return k线种数
     */
    Integer startSyncRealTimeKLine(RunEnv runEnv, TradeType tradeType);

}
