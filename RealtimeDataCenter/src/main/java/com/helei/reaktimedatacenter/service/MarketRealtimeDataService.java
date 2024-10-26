package com.helei.reaktimedatacenter.service;




public interface MarketRealtimeDataService {


    /**
     * 开始同步实时k线
     * @return k线种数
     */
    Integer startSyncRealTimeKLine();

}
