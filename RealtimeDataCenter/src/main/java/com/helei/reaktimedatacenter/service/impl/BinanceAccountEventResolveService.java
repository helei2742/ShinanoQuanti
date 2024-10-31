package com.helei.reaktimedatacenter.service.impl;


import com.helei.binanceapi.dto.accountevent.*;
import com.helei.dto.BalanceInfo;
import com.helei.dto.account.AccountBalanceInfo;
import com.helei.dto.account.AccountPositionInfo;
import com.helei.dto.account.PositionInfo;
import com.helei.dto.account.UserAccountInfo;
import com.helei.reaktimedatacenter.manager.BinanceAccountClientManager;
import com.helei.reaktimedatacenter.mapper.BalanceInfoMapper;
import com.helei.reaktimedatacenter.mapper.PositionInfoMapper;
import com.helei.reaktimedatacenter.service.AccountEventResolveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BinanceAccountEventResolveService implements AccountEventResolveService {

    private final ExecutorService eventExecutor = Executors.newVirtualThreadPerTaskExecutor();


    @Autowired
    private BinanceAccountClientManager binanceAccountClientManager;


    @Override
    public void resolveAccountEvent(UserAccountInfo accountInfo, AccountEvent accountEvent) {
        CompletableFuture<Void> future = null;

        if (accountEvent instanceof ListenKeyExpireEvent listenKeyExpireEvent) {
            future = resolveListenKeyExpireEvent(accountInfo, listenKeyExpireEvent);
        } else if (accountEvent instanceof BailNeedEvent bailNeedEvent) {
            future = resolveBailNeedEvent(accountInfo, bailNeedEvent);
        } else if (accountEvent instanceof BalancePositionUpdateEvent balancePositionUpdateEvent) {
            future = resolveBalancePositionUpdateEvent(accountInfo, balancePositionUpdateEvent);
        }


        if (future == null) return;


        future.whenCompleteAsync((unused, throwable) -> {
            if (throwable != null) {
                log.error("处理accountId[{}]事件[{}}发生错误", accountInfo.getId(), accountEvent, throwable);
            }
        }, eventExecutor);
    }

    /**
     * 处理账户仓位更新事件
     *
     * @param accountInfo                accountInfo
     * @param balancePositionUpdateEvent balancePositionUpdateEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveBalancePositionUpdateEvent(UserAccountInfo accountInfo, BalancePositionUpdateEvent balancePositionUpdateEvent) {

        // TODO 更新逻辑有误
        return CompletableFuture.runAsync(() -> {
            // 1.更新仓位信息
            AccountPositionInfo accountPositionInfo = accountInfo.getAccountPositionInfo();
            accountPositionInfo.lock();
            try {
                List<BalancePositionUpdateEvent.PositionChangeInfo> positionChangeInfos = balancePositionUpdateEvent.getPositionChangeInfos();
                List<PositionInfo> positionInfos = PositionInfoMapper.INSTANCE.convertFromPositionChangeInfoList(positionChangeInfos);

                accountPositionInfo.updatePositionInfos(positionInfos);
                accountPositionInfo.setUpdateTime(System.currentTimeMillis());
            } finally {
                accountPositionInfo.unlock();
            }


            // 2.更新资金信息
            AccountBalanceInfo accountBalanceInfo = accountInfo.getAccountBalanceInfo();
            accountBalanceInfo.lock();
            try {
                List<BalancePositionUpdateEvent.BalanceChangeInfo> balanceChangeInfos = balancePositionUpdateEvent.getBalanceChangeInfos();
                List<BalanceInfo> balanceInfos = BalanceInfoMapper.INSTANCE.convertFromBalanceChangeInfoList(balanceChangeInfos);

                accountBalanceInfo.updateBalanceInfos(balanceInfos);
                accountPositionInfo.setUpdateTime(System.currentTimeMillis());
            } finally {
                accountBalanceInfo.unlock();
            }

            log.info("accountId[{}]信息更新成功，[{}]", accountInfo.getId(), accountInfo);
        }, eventExecutor);
    }


    /**
     * 处理追加保证金事件
     *
     * @param accountInfo   accountInfo
     * @param bailNeedEvent bailNeedEvent
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> resolveBailNeedEvent(UserAccountInfo accountInfo, BailNeedEvent bailNeedEvent) {
        return CompletableFuture.runAsync(() -> {
            log.warn("账户userId[{}]-accountId[{}]追加保证金事件, 详情:[{}]", accountInfo.getUserId(), accountInfo.getId(), bailNeedEvent);

        }, eventExecutor);
    }


    /**
     * listenKey过期了，要重新获取
     *
     * @param listenKeyExpireEvent listenKeyExpireEvent
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> resolveListenKeyExpireEvent(UserAccountInfo userAccountInfo, ListenKeyExpireEvent listenKeyExpireEvent) {
        long accountId = userAccountInfo.getId();
        log.info("收到accountId[{}]的账户listenKey过期事件，尝试重新连接", accountId);

        return CompletableFuture.runAsync(() -> {
            boolean result = binanceAccountClientManager.startAccountEventStream(accountId);
            if (result) {
                log.info("accountId[{}]的账户重新获取listenKey成功", accountId);
            } else {
                // TODO 日志上传
                log.error("accountId[{}]的账户重新获取listenKey失败", accountId);
            }
        }, eventExecutor);
    }
}
