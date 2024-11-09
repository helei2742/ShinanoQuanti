package com.helei.binanceapi;

import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.dto.account.UserAccountInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


@Slf4j
public class BinanceWSAccountEventStreamClient extends AbstractBinanceWSApiClient {

    /**
     * baseUrl
     */
    private String baseUrl;

    /**
     * 账户消息流的listenKey
     */
    @Getter
    private String listenKey;

    /**
     * 账户的ASKey
     */
    @Getter
    private UserAccountInfo userAccountInfo;


    public BinanceWSAccountEventStreamClient(
            String baseUrl,
            IpWeightSupporter ipWeightSupporter
    ) throws URISyntaxException {
        super(BinanceWSClientType.ACCOUNT_STREAM, baseUrl, ipWeightSupporter, new BinanceWSAccountStreamClientHandler());
        this.baseUrl = baseUrl;
    }

    @Override
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("账户事件推送流客户端, 调用connect()方法不会启动");
            return true;
        });
    }

    public void startAccountEventStream(String listenKey, UserAccountInfo userAccountInfo, Consumer<AccountEvent> whenReceive) {
        this.userAccountInfo = userAccountInfo;

        this.listenKey = listenKey;
        super.url = baseUrl + "/" + listenKey;

        try {
            //设置回调函数
            ((BinanceWSAccountStreamClientHandler) super.handler).setWhenReceiveEvent(whenReceive);

            //开始连接
            super.connect().get();
            log.info("币安账户信息流已开启，listenKey = [{}]", listenKey);
        } catch (Exception e) {
            log.error("连接币安服务器[{}}发生错误", url, e);
            throw new RuntimeException(String.format("连接币安服务器[%s]发生错误", url), e);
        }
    }

    /**
     * 获取得到事件的回调函数
     *
     * @return 回调函数
     */
    public Consumer<AccountEvent> getEventCallback() {
        return ((BinanceWSAccountStreamClientHandler) super.handler).getWhenReceiveEvent();
    }
}
