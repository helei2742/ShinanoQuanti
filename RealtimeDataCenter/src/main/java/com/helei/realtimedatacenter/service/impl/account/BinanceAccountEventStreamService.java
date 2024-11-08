package com.helei.realtimedatacenter.service.impl.account;

import com.helei.binanceapi.BinanceWSAccountEventStreamClient;
import com.helei.binanceapi.dto.accountevent.AccountEvent;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;
import com.helei.realtimedatacenter.manager.BinanceAccountClientManager;
import com.helei.realtimedatacenter.service.AccountEventResolveService;
import com.helei.realtimedatacenter.service.AccountEventStreamService;
import com.helei.realtimedatacenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 币安账户事件流服务
 */
@Slf4j
@Service
public class BinanceAccountEventStreamService implements AccountEventStreamService {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private static final int ACCOUNT_STREAM_START_TIMES_LIMIT = 1;


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

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0]))
                .whenCompleteAsync((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("获取账户事件流发送错误", throwable);
                    } else {
                        log.info("所有用户账户事件流获取完毕，共[{}]个用户", list.size());
                    }
                }, executor);
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

        try {
            CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[0]))
                    .whenCompleteAsync((unused, throwable) -> {
                        if (throwable != null) {
                            log.error("用户id[{}]-name[{}]所有账户事件流开启时发生错误", userInfo.getId(), userInfo.getUsername(), throwable);
                        } else {
                            log.info("用户id[{}]-name[{}]所有账户事件流[共{}个]开启成功", userInfo.getId(), userInfo.getUsername(), futures.size());
                        }
                    })
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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

        return CompletableFuture
                //1 获取流客户端
                .runAsync(() -> {
                    BinanceWSAccountEventStreamClient binanceWSAccountStreamClient = null;

                    //1.1 带重试
                    for (int i = 1; i <= ACCOUNT_STREAM_START_TIMES_LIMIT; i++) {
                        try {
                            log.info("第 [{}] 次获取账户事件流, userId[{}], accountId[{}]", i, userId, accountId);

                            //1.2 创建 binanceWSAccountStreamClient 用于开启事件流
                            binanceWSAccountStreamClient = binanceAccountClientManager.startAccountEventStreamClient(accountInfo, this::resolveAccountEvent);

                            log.info("第 [{}] 次获取账户事件流成功, userId[{}], accountId[{}], listenKey[{}]", i, userId, accountId, binanceWSAccountStreamClient.getListenKey());

                        } catch (Exception e) {
                            log.error("第 [{}] 次获取账户事件流失败， userId[{}], accountId[{}]", i, userId, accountId, e);

                            binanceAccountClientManager.removeAccountStreamClient(accountId);

                            if (i == ACCOUNT_STREAM_START_TIMES_LIMIT) {
                                log.error("重试次数 [{}] 超过了限制[{}], 不再继续重试, userId[{}], accountId[{}]", i, ACCOUNT_STREAM_START_TIMES_LIMIT, userId, accountId);
                            }
                        }
                    }
                }, executor);
    }


    @Override
    public void resolveAccountEvent(final UserAccountInfo accountInfo, AccountEvent accountEvent) {
        log.info("账户[{}]-[{}]收到事件 [{}]", accountInfo.getUserId(), accountInfo.getId(), accountEvent);
        accountEventResolveService.resolveAccountEvent(accountInfo, accountEvent);
    }

}
