package com.helei.cexapi.constants;

public class WebSocketUrl {


    /**
     * query - response类型的接口
     */
    public static final String WS_NORMAL_URL = "wss://ws-api.binance.com:443/ws-api/v3";

    /**
     * web socket api 的url, 实时推送
     */
    public static final String WS_STREAM_URL = "wss://fstream.binance.com/stream";

    /**
     * 现货测试网的url
     */
    public static final String SPOT_TEST_URL = "wss://testnet.binance.vision/ws-api/v3";
}
