package com.helei.reaktimedatacenter.service.impl;

import com.helei.binanceapi.BinanceWSAccountStreamClient;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.cexapi.CEXApiFactory;
import com.helei.cexapi.client.BinanceAccountMergeClient;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;
import com.helei.reaktimedatacenter.service.AccountEventStreamService;
import com.helei.reaktimedatacenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
public class BinanceAccountEventStreamService implements AccountEventStreamService {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private static final int ACCOUNT_STREAM_START_TIMES_LIMIT = 3;

    private final BinanceApiConfig binanceApiConfig = BinanceApiConfig.INSTANCE;

    @Autowired
    private UserService userService;

    /**
     * 开启所有用户事件流
     */
    @Override
    public void startAllUserInfoEventStream() {
        log.info("开始获取所有用户的账户事件流");

        List<CompletableFuture<Void>> list = userService.queryAll().stream()
                .map(userInfo -> CompletableFuture.runAsync(() -> startUserInfoEventStream(userInfo), executor)).toList();
        list.forEach(CompletableFuture::join);

        log.info("所有用户账户事件流获取完毕，共[{}]个用户", list.size());
    }


    /**
     * 开启用户信息事件，会开启用户名下所有账户的事件流
     * @param userInfo userInfo
     */
    @Override
    public void startUserInfoEventStream(UserInfo userInfo) {
        log.info("开始获取用户id[{}]-name[{}]拥有账户事件流", userInfo.getId(), userInfo.getUsername());

        List<UserAccountInfo> accountInfos = userInfo.getAccountInfos();

        if (accountInfos == null || accountInfos.isEmpty()) {
            log.warn("用户[{}]-id[{}]没有注册交易账户事件", userInfo.getUsername(), userInfo.getId());
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UserAccountInfo accountInfo : accountInfos) {
            CompletableFuture<Void> future = buildAndStartAccountEventStream(accountInfo, 1);
            futures.add(future);
        }

        futures.forEach(CompletableFuture::join);

        log.info("用户id[{}]-name[{}]所有账户事件流[共{}个]开启成功", userInfo.getId(), userInfo.getUsername(), futures.size());
    }

    /**
     * 构建并启动账户事件流，
     * @param accountInfo accountInfo
     * @param times times
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> buildAndStartAccountEventStream(UserAccountInfo accountInfo, int times) {
        long userId = accountInfo.getUserId();
        long accountId = accountInfo.getId();

        RunEnv runEnv = accountInfo.getRunEnv();
        TradeType tradeType = accountInfo.getTradeType();

        BinanceApiConfig.BinanceTypedUrl envUrlSet = binanceApiConfig.getEnvUrlSet(runEnv, tradeType);

        log.info("开始获取账户事件流, userId[{}], accountId[{}], runEvn[{}], tradeType[{}]", userId, accountId, runEnv, tradeType);

        CompletableFuture<Void> future = CompletableFuture
                //1 获取流客户端
                .supplyAsync(() -> {
                    BinanceWSAccountStreamClient binanceWSAccountStreamClient = null;

                    //1.1 带重试
                    for (int i = 0; i < ACCOUNT_STREAM_START_TIMES_LIMIT; i++) {
                        try {
                            log.info("第 [{}] 次获取账户事件流, userId[{}], accountId[{}]", i, userId, accountId);

                            //1.2 创建 binanceWSAccountStreamClient 用于开启事件流
                            binanceWSAccountStreamClient = buildAccountInfoStream(envUrlSet, accountInfo);

                            log.info("第 [{}] 次获取账户事件流成功, userId[{}], accountId[{}], listenKey[{}]", i, userId, accountId, binanceWSAccountStreamClient.getListenKey());

                            return binanceWSAccountStreamClient;
                        } catch (Exception e) {
                            log.error("第 [{}] 次获取账户事件流失败， userId[{}], accountId[{}]", i, userId, accountId, e);

                            if (i == ACCOUNT_STREAM_START_TIMES_LIMIT - 1) {
                                log.error("重试次数 [{}] 超过了限制[{}], 不再继续重试, userId[{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, userId, accountId);
                            }
                        }
                    }

                    return binanceWSAccountStreamClient;
                }, executor)
                //2 开启流客户端
                .thenAcceptAsync(binanceWSAccountStreamClient -> {
                    // 开启事件流
                    startAccountEventStream(accountInfo, times + 1, binanceWSAccountStreamClient);
                }, executor);

        return future;
    }


    /**
     * 开始账户事件流，通过 binanceWSAccountStreamClient 进行获取
     * @param accountInfo accountInfo
     * @param times times
     * @param binanceWSAccountStreamClient binanceWSAccountStreamClient
     */
    private void startAccountEventStream(UserAccountInfo accountInfo,
                                         int times,
                                         BinanceWSAccountStreamClient binanceWSAccountStreamClient) {
        long userId = accountInfo.getUserId();
        long accountId = accountInfo.getId();

        try {
            Boolean success = binanceWSAccountStreamClient.startAccountInfoStream().get();

            if (success) {
                log.info("开启账户事件流成功，userId[{}], accountId[{}], listenKey [{}]", userId, accountId, binanceWSAccountStreamClient.getListenKey());
                return;
            } else {
                log.error("开启账户事件流失败， 重新开始获取, userId[{}], accountId[{}]", userId, accountId);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("开启账户事件流发生错误， 重新开始获取, userId[{}], accountId[{}]", userId, accountId, e);
        }

        if (times <= ACCOUNT_STREAM_START_TIMES_LIMIT) {
            log.warn("开始账户事件流重试 [{}/{}], userId[{}], accountId[{}]", times, ACCOUNT_STREAM_START_TIMES_LIMIT, userId, accountId);

            buildAndStartAccountEventStream(accountInfo, times);
        } else {
            log.error("开启账户事件流失败，并且重试次数[{}]超过限制[{}], userId[{}], accountId[{}]", times, ACCOUNT_STREAM_START_TIMES_LIMIT, userId, accountId);
        }
    }


    /**
     * 创建 BinanceWSAccountStreamClient，用来获取账户事件推送流
     * @param envUrlSet 账户环境可选择的url
     * @param accountInfo accountInfo
     * @return BinanceWSAccountStreamClient
     * @throws Exception Exception
     */
    private BinanceWSAccountStreamClient buildAccountInfoStream(BinanceApiConfig.BinanceTypedUrl envUrlSet, final UserAccountInfo accountInfo) throws Exception {
        ASKey asKey = accountInfo.getAsKey();

        // 1. 创建用于获取 listenKey 的客户端
        BinanceWSApiClient requestClient = CEXApiFactory.binanceApiClient(envUrlSet.getWs_market_url(), "账户事件请求链接");

        // 2, 创建用于获取 merge 客户端
        BinanceAccountMergeClient accountMergeClient = new BinanceAccountMergeClient(requestClient, envUrlSet.getWs_account_info_stream_url());

        // 3. 开始根据 merge 客户端，获取该账户的推送流链接
        BinanceWSAccountStreamClient binanceWSAccountStreamClient = accountMergeClient.addAccountStream(asKey, accountEvent -> resolveAccountEvent(accountInfo, accountEvent));

        InetSocketAddress proxy = binanceApiConfig.getProxy();
        if (proxy != null) {
            binanceWSAccountStreamClient.setProxy(proxy);
        }

        return binanceWSAccountStreamClient;

    }


    @Override
    public void resolveAccountEvent(final UserAccountInfo accountInfo, AccountEvent accountEvent) {

        log.info("账户[{}]-[{}]收到事件 [{}]", accountInfo.getUserId(), accountInfo.getId(), accountEvent);
    }
}
