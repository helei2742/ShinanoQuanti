package com.helei.binanceapi.constants;

public class BinanceApiUrl {


    /**
     * query - response类型的接口
     */
    public static final String WS_NORMAL_URL = "wss://ws-api.binance.com:443/ws-api/v3";
    public static final String WS_NORMAL_URL_TEST = "wss://testnet.binancefuture.com/ws-fapi/v1";


    /**
     * u本位合约账户信息推送url
     */
    public static final String WS_ACCOUNT_INFO_STREAM_URL = "https://fapi.binance.com";
    public static final String WS_ACCOUNT_INFO_STREAM_URL_TEST = "wss://fstream.binancefuture.com/ws";

    /**
     * u本位合约的市场数据推送url
     */
    public static final String WS_U_CONTRACT_STREAM_URL = "wss://fstream.binance.com";

    /**
     * web socket api 的url, 实时推送
     */
    public static final String WS_SPOT_STREAM_URL = "wss://fstream.binance.com/stream";

    /**
     * 现货测试网的url
     */
    public static final String SPOT_TEST_URL = "wss://testnet.binance.vision/ws-api/v3";
}
