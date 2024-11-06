package com.helei.binanceapi;

import com.helei.binanceapi.api.ws.BinanceWSBaseApi;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.dto.ASKey;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 币安账户信息流推送客户端
 */
@Slf4j
public class BinanceWSAccountStreamClient extends AbstractBinanceWSApiClient {


    /**
     * stream url
     */
    private final String streamUrl;

    /**
     * 账户信息的asKey
     */
    @Getter
    private final ASKey asKey;

    /**
     * BaseApi，用来维护listenKey
     */
    private final BinanceWSBaseApi baseApi;

    /**
     * 当前的listenKey
     */
    @Getter
    private String listenKey;

    /**
     * 收到信息的回调
     */
    @Getter
    private final Consumer<AccountEvent> whenReceiveEvent;


    public BinanceWSAccountStreamClient(
            String streamUrl,
            IpWeightSupporter ipWeightSupporter,
            ASKey asKey,
            Consumer<AccountEvent> whenReceiveEvent,
            BinanceWSBaseApi baseApi
    ) throws URISyntaxException {
        super(streamUrl, ipWeightSupporter, null, new BinanceWSAccountStreamClientHandler(whenReceiveEvent));
        this.streamUrl = streamUrl;
        this.whenReceiveEvent = whenReceiveEvent;
        this.asKey = asKey;
        this.baseApi = baseApi;

        setName("币安账户信息推送客户端-" + asKey.getApiKey().substring(0, 8));
    }

    /**
     * 开始获取账户信息流
     *
     * @return CompletableFuture<Boolean> 是否成功
     */
    public CompletableFuture<Boolean> startAccountInfoStream() {
        log.info("开始获取账户信息流， apiKey = [{}]", asKey.getApiKey());

        return baseApi
                .requestListenKey(asKey)//获取listenKey
                .thenApplyAsync(listenKey -> { //请求ws连接
                    log.info("listenKey = [{}]", listenKey);
                    if (listenKey == null) {
                        log.error("获取listenKey结果为null，");
                        return false;
                    }
                    this.listenKey = listenKey;
                    super.url = streamUrl + "/" + listenKey;
                    try {
                        connect().get();
                        log.info("账户信息流已开启，listenKey = [{}]", listenKey);
                        return true;
                    } catch (Exception e) {
                        log.error("连接服务器[{}}发生错误", url, e);
                    }
                    return false;
                });
    }


    /**
     * 延长listenKey的时间
     *
     * @return future
     */
    public CompletableFuture<String> lengthenListenKey() {
        return baseApi.lengthenListenKey(listenKey, asKey);
    }

}
