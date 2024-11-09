package com.helei.realtimedatacenter.manager;

import com.helei.binanceapi.BinanceWSAccountEventStreamClient;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.RunEnv;
import com.helei.constants.WebsocketClientStatus;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.base.KeyValue;
import com.helei.dto.account.UserAccountInfo;
import com.helei.realtimedatacenter.config.RealtimeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
        import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


@Slf4j
@Component
public class BinanceAccountEventClientManager implements InitializingBean {

    private final static long REFRESH_TASK_SLEEP_TIME = 60000;

    private final static long REFRESH_INTERVAL = 1000 * 60 * 10;

    private final static int REFRESH_CONCURRENT_LIMIT = 10;

    private static final int ACCOUNT_STREAM_START_TIMES_LIMIT = 1;

    private final static ConcurrentMap<Long, KeyValue<BinanceWSAccountEventStreamClient, Long>> accountIdMapClientAndExpireMap = new ConcurrentHashMap<>();

    private final RealtimeConfig realtimeConfig = RealtimeConfig.INSTANCE;

    private final AtomicBoolean streamRefreshTaskState = new AtomicBoolean(true);

    private final ExecutorService executor;

    @Autowired
    private BinanceBaseClientManager binanceBaseClientManager;


    @Autowired
    public BinanceAccountEventClientManager(ExecutorServiceManager executorServiceManager) {
        this.executor = executorServiceManager.getAccountRTDataExecutor();
    }

    /**
     * 创建 BinanceWSAccountEventStreamClient，用来获取账户事件推送流
     * <P>创建完毕后会自动启动，并注册延长ListenKey</P>
     *
     * @param accountInfo accountInfo
     * @return BinanceWSAccountStreamClient
     * @throws Exception Exception
     */
    public BinanceWSAccountEventStreamClient startAccountEventStreamClient(final UserAccountInfo accountInfo, BiConsumer<UserAccountInfo, AccountEvent> whenReceiveAccountEvent) throws Exception {

        doCreateAccountEventStreamClient(accountInfo, accountEvent -> whenReceiveAccountEvent.accept(accountInfo, accountEvent)).get();

        return accountIdMapClientAndExpireMap.get(accountInfo.getId()).getKey();
    }


    private CompletableFuture<Void> doCreateAccountEventStreamClient(UserAccountInfo accountInfo, Consumer<AccountEvent> whenReceiveAccountEvent) {
        RunEnv runEnv = accountInfo.getRunEnv();
        TradeType tradeType = accountInfo.getTradeType();
        ASKey asKey = accountInfo.getAsKey();
        long accountId = accountInfo.getId();
        long userId = accountInfo.getUserId();

        // 1. 得到用于获取 listenKey 的客户端
        CompletableFuture<AbstractBinanceWSApiClient> requestClientFuture = binanceBaseClientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.REQUEST_RESPONSE);

        // 2.获取用于账户事件传输的客户端
        String mark = String.format("账户事件推送客户端[%s]-[%s]", userId, accountId);
        CompletableFuture<AbstractBinanceWSApiClient> streamClientFuture = binanceBaseClientManager.getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.ACCOUNT_STREAM, mark);

        // 3,等待两个客户端都获取完成, 调用requestClient获取listenKey后启动账户事件传输客户端
        return CompletableFuture
                .allOf(new CompletableFuture[]{requestClientFuture, streamClientFuture})
                .whenCompleteAsync((unused, throwable) -> {
                    if (throwable != null) {
                        throw new RuntimeException("获取" + mark + "发生错误", throwable);
                    }

                    try {
                        BinanceWSReqRespApiClient reqRespApiClient = (BinanceWSReqRespApiClient) requestClientFuture.get();
                        BinanceWSAccountEventStreamClient accountEventStreamClient = (BinanceWSAccountEventStreamClient) streamClientFuture.get();

                        accountIdMapClientAndExpireMap.compute(accountId, (k, v) -> {
                            if (v == null) {
                                try {
                                    //刷新时间ddl设置为特殊值0代表没有启动
                                    v = new KeyValue<>(accountEventStreamClient, 0L);
                                } catch (Exception e) {
                                    throw new RuntimeException("创建币按账户推送流失", e);
                                }
                            }
                            return v;
                        });

                        if (accountEventStreamClient.getClientStatus().equals(WebsocketClientStatus.RUNNING)) {
                            log.warn("{}已经在运行，不能重复启动", mark);
                            return;
                        }


                        reqRespApiClient.getBaseApi()
                                // 3.1 获取listenKey
                                .requestListenKey(asKey)
                                // 3.2 启动流
                                .thenAcceptAsync(listenKey -> accountEventStreamClient.startAccountEventStream(listenKey, accountInfo, whenReceiveAccountEvent), executor)
                                .get();

                        // 3.3 注册到ExpireMap，以完成自动刷新ListenKey
                        accountIdMapClientAndExpireMap.get(accountId).setValue(System.currentTimeMillis());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
    }


    /**
     * 根据accountId删除accountIdMapClientAndExpireMap里缓存的client
     *
     * @param accountId accountId
     */
    public void removeAccountStreamClient(long accountId) {
        KeyValue<BinanceWSAccountEventStreamClient, Long> remove = accountIdMapClientAndExpireMap.remove(accountId);
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
    public boolean restartAccountEventStream(long accountId) {
        KeyValue<BinanceWSAccountEventStreamClient, Long> pair = accountIdMapClientAndExpireMap.get(accountId);

        if (pair == null) return false;

        return restartAccountEventStream(accountId, pair.getKey());
    }


    /**
     * 开始账户事件流，通过 binanceWSAccountStreamClient 进行获取
     * <p>带重试次数，未达到重试限制之前，都会一直重试</p>
     *
     * @param accountId                    accountId
     * @param binanceWSAccountStreamClient binanceWSAccountStreamClient
     */
    private boolean restartAccountEventStream(long accountId,
                                              BinanceWSAccountEventStreamClient binanceWSAccountStreamClient) {

        for (int i = 1; i <= ACCOUNT_STREAM_START_TIMES_LIMIT; i++) {
            try {
                doCreateAccountEventStreamClient(binanceWSAccountStreamClient.getUserAccountInfo(), binanceWSAccountStreamClient.getEventCallback()).get();
                return true;
            } catch (InterruptedException | ExecutionException e) {
                log.error("开启账户事件流发生错误[{}/{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, accountId, e);
            }

            log.warn("开始账户事件流重试 [{}/{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, accountId);
        }

        log.error("accountId[{}] 重试次数[{}]超过限制[{}],", accountId, ACCOUNT_STREAM_START_TIMES_LIMIT, ACCOUNT_STREAM_START_TIMES_LIMIT);
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
                for (Map.Entry<Long, KeyValue<BinanceWSAccountEventStreamClient, Long>> entry : accountIdMapClientAndExpireMap.entrySet()) {
                    Long accountId = entry.getKey();

                    KeyValue<BinanceWSAccountEventStreamClient, Long> keyValue = entry.getValue();
                    BinanceWSAccountEventStreamClient client = keyValue.getKey();
                    Long expireTime = keyValue.getValue();

                    //没启动的，特殊特殊处理，跳过刷新listenKey
                    if (expireTime == 0L) continue;

                    semaphore.acquire();

                    if (System.currentTimeMillis() <= expireTime) {
                        log.info("accountId[{}]的事件流到了刷新时间，开始延长listenKey[{}]", accountId, client.getListenKey());

                        RunEnv runEnv = client.getRunEnv();
                        TradeType tradeType = client.getTradeType();

                        binanceBaseClientManager
                                // 1. 得到用于刷新 listenKey 的客户端
                                .getEnvTypedApiClient(runEnv, tradeType, BinanceWSClientType.REQUEST_RESPONSE)
                                // 2. 刷新listenKey
                                .thenAcceptAsync(requestClient -> {
                                    BinanceWSReqRespApiClient reqRespApiClient = (BinanceWSReqRespApiClient) requestClient;
                                    reqRespApiClient.getBaseApi()
                                            // 2.1 请求延长listenKey
                                            .lengthenListenKey(client.getListenKey(), client.getUserAccountInfo().getAsKey())
                                            // 2.2 延长listenKey有错误，重新获取
                                            .whenCompleteAsync((key, throwable) -> {
                                                try {
                                                    if (throwable != null) {
                                                        log.error("延长accountId[{}]的listenKey[{}]发生错误, 重新启动account链接", accountId, client.getListenKey(), throwable);
                                                        //重新获取listenKey启动
                                                        boolean result = restartAccountEventStream(accountId, client);
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
                                            });
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


    public void close() {
        streamRefreshTaskState.set(false);
        executor.shutdown();
    }
}


