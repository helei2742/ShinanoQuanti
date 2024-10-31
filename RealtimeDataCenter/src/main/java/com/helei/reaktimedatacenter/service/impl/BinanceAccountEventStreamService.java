package com.helei.reaktimedatacenter.service.impl;

import com.helei.binanceapi.BinanceWSAccountStreamClient;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;
import com.helei.reaktimedatacenter.manager.BinanceAccountClientManager;
import com.helei.reaktimedatacenter.service.AccountEventResolveService;
import com.helei.reaktimedatacenter.service.AccountEventStreamService;
import com.helei.reaktimedatacenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
public class BinanceAccountEventStreamService implements AccountEventStreamService {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private static final int ACCOUNT_STREAM_START_TIMES_LIMIT = 3;


    @Autowired
    private UserService userService;

    @Autowired
    private AccountEventResolveService accountEventResolveService;


    @Autowired
    private BinanceAccountClientManager binanceAccountClientManager;


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
     *
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
            CompletableFuture<Void> future = buildAndStartAccountEventStream(accountInfo);
            futures.add(future);
        }

        futures.forEach(CompletableFuture::join);

        log.info("用户id[{}]-name[{}]所有账户事件流[共{}个]开启成功", userInfo.getId(), userInfo.getUsername(), futures.size());
    }

    /**
     * 构建并启动账户事件流，
     *
     * @param accountInfo accountInfo
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> buildAndStartAccountEventStream(UserAccountInfo accountInfo) {
        long userId = accountInfo.getUserId();
        long accountId = accountInfo.getId();

        RunEnv runEnv = accountInfo.getRunEnv();
        TradeType tradeType = accountInfo.getTradeType();


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
                            binanceWSAccountStreamClient = binanceAccountClientManager.getAccountInfoStream(accountInfo, this::resolveAccountEvent);

                            log.info("第 [{}] 次获取账户事件流成功, userId[{}], accountId[{}], listenKey[{}]", i, userId, accountId, binanceWSAccountStreamClient.getListenKey());

                            return binanceWSAccountStreamClient;
                        } catch (Exception e) {
                            log.error("第 [{}] 次获取账户事件流失败， userId[{}], accountId[{}]", i, userId, accountId, e);

                            binanceAccountClientManager.removeAccountStreamClient(accountId);

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
                    startAccountEventStream(accountInfo, binanceWSAccountStreamClient);
                }, executor);

        return future;
    }


    /**
     * 开始账户事件流，通过 binanceWSAccountStreamClient 进行获取
     *
     * @param accountInfo                  accountInfo
     * @param binanceWSAccountStreamClient binanceWSAccountStreamClient
     */
    private void startAccountEventStream(UserAccountInfo accountInfo,
                                         BinanceWSAccountStreamClient binanceWSAccountStreamClient) {
        long accountId = accountInfo.getId();

        boolean success = binanceAccountClientManager.startAccountEventStream(accountId, binanceWSAccountStreamClient);

        if (success) {
            log.info("开启账户事件流成功, accountId[{}], listenKey [{}]", accountId, binanceWSAccountStreamClient.getListenKey());
        } else {
            // TODO 错误日志上传
            log.error("开启账户事件流失败, accountId[{}]", accountId);
        }
    }


    @Override
    public void resolveAccountEvent(final UserAccountInfo accountInfo, AccountEvent accountEvent) {
        log.info("账户[{}]-[{}]收到事件 [{}]", accountInfo.getUserId(), accountInfo.getId(), accountEvent);
        accountEventResolveService.resolveAccountEvent(accountInfo, accountEvent);
    }
}
