package com.helei.cexapi.client;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.helei.binanceapi.BinanceWSAccountStreamClient;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.dto.ASKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.*;
        import java.util.function.Consumer;


/**
 * 币安账户综合客户端
 * <P>主要由两部分组成</P>
 * <p>一是用于请求维护listenKey的BinanceWSApiClient</p>
 * <p>
 * 二是ConcurrentMap< ASKey, BinanceWSAccountStreamClient>
 * 里面记录了账户鉴权信息和对应的账户信息推送连接BinanceWSAccountStreamClient
 * </p>
 */
@Deprecated
@Slf4j
public class BinanceAccountMergeClient {

    /**
     * 处理相关异步任务的线程池
     */
    @Getter
    private final ExecutorService executor;

    /**
     * 用于请求listenKey，更新listenKey等请求的WSClient
     */
    private final BinanceWSReqRespApiClient requestClient;

    /**
     * 账户信息推送的base url
     */
    private final String accountStreamUrl;

    /**
     * 代理
     */
    private InetSocketAddress proxy;


    public BinanceAccountMergeClient(
            BinanceWSReqRespApiClient requestClient,
            String accountStreamUrl,
            ExecutorService executor

    ) throws Exception {
        this.requestClient = requestClient;
        if (StrUtil.isBlank(requestClient.getName())) {
            this.requestClient.setName("账户信息请求客户端-" + UUID.randomUUID().toString().substring(0, 8));
        }

        this.accountStreamUrl = accountStreamUrl;
        this.executor = executor;

        log.info("开始连接请求服务器");
        requestClient.connect().get();
    }


    public void setProxy(InetSocketAddress proxy) {
        this.proxy = proxy;
        requestClient.setProxy(proxy);
    }


    /**
     * 添加账户流，根据asKey创建BinanceWSAccountStreamClient对象进行连接，并绑定收到事件的回调。
     *
     * @param asKey                   asKey
     * @param whenReceiveAccountEvent channel收到事件消息时的回调
     * @return this
     * @throws Exception exception
     */
    public BinanceWSAccountStreamClient getAccountStream(ASKey asKey, Consumer<AccountEvent> whenReceiveAccountEvent) throws Exception {

        try {
            BinanceWSAccountStreamClient client = new BinanceWSAccountStreamClient(
                    accountStreamUrl,
                    new IpWeightSupporter("localIp"),
                    asKey,
                    whenReceiveAccountEvent,
                    requestClient.getBaseApi()
            );
            client.setProxy(proxy);

            return client;
        } catch (Exception e) {
            log.error("创建account连接出错, apiKey[{}]", asKey.getApiKey(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 刷新BinanceWSAccountStreamClient
     *
     * @param streamClient streamClient
     * @return boolean
     */
    private static boolean refreshClient(BinanceWSAccountStreamClient streamClient) {
        try {
            String s = streamClient.lengthenListenKey().get();
            if (s == null) {
                log.error("延长listenKey[{}]得到结果为null", streamClient.getListenKey());
                return false;
            }
            return true;
        } catch (InterruptedException | ExecutionException e) {
            log.error("延长listenKey[{}]发生错误", streamClient.getListenKey(), e);
            return false;
        }
    }


    /**
     * 重连接账户信息推送流
     *
     * @param streamClient streamClient
     * @param retryTimes   retryTimes
     */
    private static boolean reConnectStream(BinanceWSAccountStreamClient streamClient, int retryTimes) {
        for (int i = 1; i <= retryTimes; i++) {
            log.info("开始重连接account stream， [{}/{}]，旧的listenKey[{}]", i, retryTimes, streamClient.getListenKey());
            Boolean b = null;
            try {

                //关闭，重置
                streamClient.close();

                //开始连接
                b = streamClient.startAccountInfoStream().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("重新连接account stream发生错误，", e);
            }
            if (BooleanUtil.isTrue(b)) {
                log.info("重连接account stream成功, 新的listenKey[{}]", streamClient.getListenKey());
                return true;
            }
        }

        log.error("重连接失败,apiKey[{}]", streamClient.getAsKey().getApiKey());
        return false;
    }
}

