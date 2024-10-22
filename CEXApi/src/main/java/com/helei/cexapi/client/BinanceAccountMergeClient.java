package com.helei.cexapi.client;

import cn.hutool.core.util.BooleanUtil;
import com.helei.binanceapi.BinanceWSAccountStreamClient;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.BinanceWSApiClientHandler;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.dto.ASKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


/**
 * 币安账户综合客户端
 * <P>主要由两部分组成</P>
 * <p>一是用于请求维护listenKey的BinanceWSApiClient</p>
 * <p>
 *     二是ConcurrentMap< ASKey, BinanceWSAccountStreamClient>
 *     里面记录了账户鉴权信息和对应的账户信息推送连接BinanceWSAccountStreamClient
 * </p>
 */
@Slf4j
public class BinanceAccountMergeClient {

    /**
     * 处理相关异步任务的线程池
     */
    @Getter
    private final VirtualThreadTaskExecutor publishExecutor = new VirtualThreadTaskExecutor();

    /**
     * 记录已有的账户信息推送连接
     */
    private final ConcurrentMap<ASKey, BinanceWSAccountStreamClient> accountStreamClientMap = new ConcurrentHashMap<>();

    /**
     * 用于请求listenKey，更新listenKey等请求的WSClient
     */
    private final BinanceWSApiClient requestClient;

    /**
     * 账户信息推送的base url
     */
    private final String accountStreamUrl;

    /**
     * 代理
     */
    private InetSocketAddress proxy;


    public BinanceAccountMergeClient(BinanceWSApiClient requestClient, String accountStreamUrl) throws Exception {
        this.requestClient = requestClient;
        this.requestClient.setName("账户信息请求客户端-" + UUID.randomUUID().toString().substring(0, 8));
        this.accountStreamUrl = accountStreamUrl;

        log.info("开始连接请求服务器");
        requestClient.connect().get();
    }

    public BinanceAccountMergeClient(String requestUrl, String accountStreamUrl) throws Exception {
        this(
                new BinanceWSApiClient(
                        requestUrl,
                        new IpWeightSupporter("localIp"),
                        new BinanceWSApiClientHandler()
                ),
                accountStreamUrl
        );
    }

    public BinanceAccountMergeClient setProxy(InetSocketAddress proxy) {
        this.proxy = proxy;
        requestClient.setProxy(proxy);
        return this;
    }


    /**
     * 添加账户流，根据asKey创建BinanceWSAccountStreamClient对象进行连接，并绑定收到事件的回调。
     *
     * @param asKey                   asKey
     * @param whenReceiveAccountEvent channel收到事件消息时的回调
     * @return this
     * @throws URISyntaxException exception
     */
    public BinanceWSAccountStreamClient addAccountStream(ASKey asKey, Consumer<AccountEvent> whenReceiveAccountEvent) throws URISyntaxException {

        BinanceWSAccountStreamClient binanceWSAccountStreamClient = accountStreamClientMap.compute(
                asKey,
                (k, v) -> {
                    if (v == null) {
                        try {
                            v = new BinanceWSAccountStreamClient(
                                    accountStreamUrl,
                                    new IpWeightSupporter("localIp"),
                                    asKey,
                                    whenReceiveAccountEvent,
                                    requestClient.getBaseApi()
                            );
                        } catch (URISyntaxException e) {
                            log.error("创建account连接出错, apiKey[{}]", asKey.getApiKey(), e);
                            throw new RuntimeException(e);
                        }
                        v.setProxy(proxy);
                    }
                    return v;
                }
        );
        log.info("创建account连接成功，apiKey[{}]", asKey.getApiKey());

        return binanceWSAccountStreamClient;
    }

    /**
     * 刷新连接
     */
    public void refreshLink() {
        for (Map.Entry<ASKey, BinanceWSAccountStreamClient> entry : accountStreamClientMap.entrySet()) {
            BinanceWSAccountStreamClient streamClient = entry.getValue();

            CompletableFuture
                    //刷新，延长listenKey
                    .supplyAsync(() -> refreshClient(streamClient), publishExecutor)
                    //失败了就尝试重连接,重连接失败，在map中去掉他
                    .thenAcceptAsync(success->{
                        if (!success) {
                            log.warn("开始重新获取listenKey并连接");
                            if (reConnectStream(streamClient, 3)) {
                                accountStreamClientMap.remove(entry.getKey());
                                log.warn("重连接次数超过限制，不再尝试重连该账户的信息流");
                            }
                        }
                    }, publishExecutor);
        }
    }

    /**
     * 刷新BinanceWSAccountStreamClient
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
     * @param streamClient streamClient
     * @param retryTimes retryTimes
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
