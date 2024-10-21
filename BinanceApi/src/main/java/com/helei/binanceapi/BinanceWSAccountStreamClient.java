package com.helei.binanceapi;


import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.api.ws.BinanceWSBaseApi;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.AccountEventType;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.dto.ASKey;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.util.CustomBlockingQueue;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

/**
 * 币安账户信息流推送客户端
 */
@Slf4j
public class BinanceWSAccountStreamClient extends AbstractBinanceWSApiClient {

    /**
     * 账户信息的asKey
     */
    private final ASKey asKey;

    /**
     * BaseApi，用来维护listenKey
     */
    private final BinanceWSBaseApi baseApi;

    /**
     * 账户信息流的缓冲区
     */
    private final CustomBlockingQueue<JSONObject> accountInfoBuffer;

    /**
     * 当前的listenKey
     */
    private String listenKey;


    public BinanceWSAccountStreamClient(
            String streamUrl,
            IpWeightSupporter ipWeightSupporter,
            ASKey asKey,
            CustomBlockingQueue<JSONObject> buffer,
            BinanceWSBaseApi baseApi
    ) throws URISyntaxException {
        super(streamUrl, ipWeightSupporter, null, new BinanceWSAccountStreamClientHandler(buffer));
        this.accountInfoBuffer = buffer;
        this.asKey = asKey;
        this.baseApi = baseApi;
    }


    public BinanceWSAccountStreamClient(
            String streamUrl,
            IpWeightSupporter ipWeightSupporter,
            ASKey asKey,
            int bufferSize,
            BinanceWSBaseApi baseApi
    ) throws URISyntaxException {
        this(streamUrl, ipWeightSupporter, asKey, new CustomBlockingQueue<>(bufferSize), baseApi);
    }


    /**
     * 开始获取账户信息流
     *
     * @return CompletableFuture<Boolean> 是否成功
     */
    public CompletableFuture<Boolean> startAccountInfoStream() {
        log.info("开始获取账户信息流， apiKey = [{}]", asKey.getApiKey());

        return baseApi.requestListenKey(asKey)//获取listenKey
                .thenApplyAsync(listenKey -> { //请求ws连接
                    log.info("listenKey = [{}]", listenKey);
                    if (listenKey == null) {
                        log.error("获取listenKey结果为null，");
                        return false;
                    }
                    this.listenKey = listenKey;
                    super.url = url + "/" + listenKey;
                    try {
                        connect().get();
                        return true;
                    } catch (Exception e) {
                        log.error("连接服务器[{}}发生错误", url, e);
                    }
                    return false;
                });
    }


    /**
     * 获取推送的账户信息
     * @return AccountEvent
     * @throws Exception Exception
     */
    public AccountEvent getAccountEvent() throws Exception {
        JSONObject take = accountInfoBuffer.take();

        if (take == null) {
            log.error("推送的账户信息不应该为null！");
            throw new IllegalArgumentException("推送的账户信息不应该为null");
        }

        String eventTypeStr = take.getString("e");
        AccountEventType eventType = AccountEventType.STATUS_MAP.get(eventTypeStr);

        AccountEvent accountEvent = eventType.getConverter().convertFromJsonObject(take);
        return accountEvent;
    }

}
