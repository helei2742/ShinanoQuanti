package com.helei.reaktimedatacenter.manager;

import com.helei.binanceapi.BinanceWSAccountStreamClient;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.client.BinanceAccountMergeClient;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.KeyValue;
import com.helei.dto.account.UserAccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;


@Slf4j
@Component
public class BinanceAccountClientManager implements InitializingBean {

    private final static long REFRESH_TASK_SLEEP_TIME = 60000;

    private final static long REFRESH_INTERVAL = 1000 * 60 * 10;

    private final static int REFRESH_CONCURRENT_LIMIT = 10;

    private static final int ACCOUNT_STREAM_START_TIMES_LIMIT = 3;


    private final ExecutorService executor;


    private final static ConcurrentMap<String, BinanceAccountMergeClient> mergeClientMap = new ConcurrentHashMap<>();


    private final static ConcurrentMap<Long, KeyValue<BinanceWSAccountStreamClient, Long>> accountIdMapClientAndExpireMap = new ConcurrentHashMap<>();


    private final BinanceApiConfig binanceApiConfig = BinanceApiConfig.INSTANCE;


    private final AtomicBoolean streamRefreshTaskState = new AtomicBoolean(true);


    @Autowired
    public BinanceAccountClientManager(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getAccountRTDataExecutor();
    }

    /**
     * 创建 BinanceWSAccountStreamClient，用来获取账户事件推送流
     *
     * @param accountInfo accountInfo
     * @return BinanceWSAccountStreamClient
     * @throws Exception Exception
     */
    public BinanceWSAccountStreamClient getAccountInfoStream(final UserAccountInfo accountInfo, BiConsumer<UserAccountInfo, AccountEvent> whenReceiveAccountEvent) throws Exception {

        RunEnv runEnv = accountInfo.getRunEnv();
        TradeType tradeType = accountInfo.getTradeType();
        ASKey asKey = accountInfo.getAsKey();


        BinanceApiConfig.BinanceTypedUrl envUrlSet = binanceApiConfig.getEnvUrlSet(runEnv, tradeType);


        // 1. 创建用于获取 listenKey 的客户端
        BinanceWSApiClient requestClient = CEXApiFactory.binanceApiClient(envUrlSet.getWs_market_url(), "账户事件请求链接");

        // 2, 获取 merge 客户端
        String key = generateMergeClientKey(runEnv, tradeType);
        String streamUrl = envUrlSet.getWs_account_info_stream_url();

        BinanceAccountMergeClient accountMergeClient = mergeClientMap.compute(key, (k, v) -> {
            if (v == null) {
                try {
                    v = new BinanceAccountMergeClient(requestClient, streamUrl, executor);

                    InetSocketAddress proxy = binanceApiConfig.getProxy();
                    if (proxy != null) {
                        v.setProxy(proxy);
                    }
                } catch (Exception e) {
                    log.error("创建BinanceAccountMergeClient发生错误, url[{}}", streamUrl, e);
                    throw new RuntimeException(e);
                }
            }

            return v;
        });


        // 3. 开始根据 merge 客户端，获取该账户的推送流链接
        KeyValue<BinanceWSAccountStreamClient, Long> clientLongKeyValue = accountIdMapClientAndExpireMap.compute(accountInfo.getId(), (k, v) -> {
            if (v == null) {
                try {
                    BinanceWSAccountStreamClient binanceWSAccountStreamClient = accountMergeClient.getAccountStream(asKey, accountEvent -> whenReceiveAccountEvent.accept(accountInfo, accountEvent));

                    //刷新时间ddl设置为特殊值0代表没有启动
                    v = new KeyValue<>(binanceWSAccountStreamClient, 0L);
                } catch (Exception e) {
                    throw new RuntimeException("创建币按账户推送流失", e);
                }
            }

            return v;
        });

        return clientLongKeyValue.getKey();
    }


    /**
     * 根据accountId删除accountIdMapClientAndExpireMap里缓存的client
     *
     * @param accountId accountId
     */
    public void removeAccountStreamClient(long accountId) {
        KeyValue<BinanceWSAccountStreamClient, Long> remove = accountIdMapClientAndExpireMap.remove(accountId);
        if (remove != null) {
            log.warn("已移除AccountStreamClient [{}]", remove.getKey().getName());
        }
    }

    /**
     * 开始账户事件流，通过 binanceWSAccountStreamClient 进行获取
     * <p>带重试次数，未达到重试限制之前，都会一直重试</p>
     *
     * @param accountId accountId
     */
    public boolean startAccountEventStream(long accountId) {
        KeyValue<BinanceWSAccountStreamClient, Long> pair = accountIdMapClientAndExpireMap.get(accountId);

        if (pair == null) return false;

        return startAccountEventStream(accountId, pair.getKey());
    }


    /**
     * 开始账户事件流，通过 binanceWSAccountStreamClient 进行获取
     * <p>带重试次数，未达到重试限制之前，都会一直重试</p>
     *
     * @param accountId                    accountId
     * @param binanceWSAccountStreamClient binanceWSAccountStreamClient
     */
    public boolean startAccountEventStream(long accountId,
                                           BinanceWSAccountStreamClient binanceWSAccountStreamClient) {

        for (int i = 0; i < ACCOUNT_STREAM_START_TIMES_LIMIT; i++) {
            try {
                Boolean success = binanceWSAccountStreamClient.startAccountInfoStream().get();

                if (success) {

                    //设置刷新时间
                    accountIdMapClientAndExpireMap.compute(accountId, (k, v) -> {
                        if (v == null)
                            v = new KeyValue<>(binanceWSAccountStreamClient, System.currentTimeMillis() + REFRESH_INTERVAL);
                        return v;
                    });

                    return true;
                } else {
                    log.error("开启账户事件流失败[{}/{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, accountId);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("开启账户事件流发生错误[{}/{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, accountId, e);
            }

            log.warn("开始账户事件流重试 [{}/{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, accountId);

        }

        log.error(" accountId[{}] 重试次数[{}]超过限制[{}],", accountId, ACCOUNT_STREAM_START_TIMES_LIMIT, ACCOUNT_STREAM_START_TIMES_LIMIT);
        return false;
    }


    /**
     * 定期刷新账户流的ListenKey
     */
    private void refreshAccountStreamListenKey() {
        while (streamRefreshTaskState.get()) {
            long startTime = System.currentTimeMillis();
            Semaphore semaphore = new Semaphore(REFRESH_CONCURRENT_LIMIT);
            try {


                // 1. 去除失效的
                List<Long> expireIdList = accountIdMapClientAndExpireMap.entrySet().stream().filter(e -> e.getValue().getValue() == -1L).map(Map.Entry::getKey).toList();

                if (!expireIdList.isEmpty()) {
                    log.warn("去除失效的客户端链接 accountIds [{}]", expireIdList);
                    expireIdList.forEach(accountIdMapClientAndExpireMap::remove);
                }

                // 2.尝试刷新listenKey
                for (Map.Entry<Long, KeyValue<BinanceWSAccountStreamClient, Long>> entry : accountIdMapClientAndExpireMap.entrySet()) {
                    Long accountId = entry.getKey();
                    KeyValue<BinanceWSAccountStreamClient, Long> keyValue = entry.getValue();
                    BinanceWSAccountStreamClient client = keyValue.getKey();
                    Long expireTime = keyValue.getValue();

                    //没启动的，特殊特殊处理，跳过刷新listenKey
                    if (expireTime == 0L) continue;

                    semaphore.acquire();

                    if (System.currentTimeMillis() <= expireTime) {
                        log.info("accountId[{}]的事件流到了刷新时间，开始延长listenKey[{}]", accountId, client.getListenKey());

                        client.lengthenListenKey().whenCompleteAsync((listenKey, e) -> {
                            try {
                                if (e != null) {
                                    log.error("延长accountId[{}]的listenKey[{}]发生错误, 重新启动account链接", accountId, client.getListenKey(), e);

                                    //重新获取listenKey启动
                                    boolean result = startAccountEventStream(accountId, client);
                                    if (!result) {
                                        // TODO 错误日志上传
                                        log.error("accountId[{}}重新获取推送链接失败", accountId);

                                        //特殊标记，下一次刷新时删掉
                                        keyValue.setValue(-1L);
                                    } else {
                                        log.info("accountId[{}]重新获取推送链接成功, listenKey[{}]", accountId, client.getListenKey());
                                        keyValue.setValue(System.currentTimeMillis() + REFRESH_INTERVAL);
                                    }
                                }
                            } finally {
                                semaphore.release();

                            }
                        }, executor);
                    }
                }
            } catch (InterruptedException e) {
                log.error("定时刷新账户流ListenKey失败");
            }

            try {
                TimeUnit.MILLISECONDS.sleep(REFRESH_TASK_SLEEP_TIME - System.currentTimeMillis() + startTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 初始化，开始刷新链接任务
     *
     * @throws Exception Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        executor.execute(this::refreshAccountStreamListenKey);
    }


    /**
     * 获取 MergeClientKey
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @return key
     */
    private String generateMergeClientKey(RunEnv runEnv, TradeType tradeType) {
        return runEnv + "." + tradeType;
    }


    public void close() {
        streamRefreshTaskState.set(false);
        executor.shutdown();
    }
}

