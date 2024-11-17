package com.helei.realtimedatacenter.realtime;

import cn.hutool.core.lang.Pair;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.constants.trade.KLineInterval;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public abstract class KLineRTDataSyncTask<T> {

    protected final List<Pair<String, KLineInterval>> listenKLines;

    protected KLineRTDataSyncTask(List<Pair<String, KLineInterval>> listenKLines) {
        this.listenKLines = listenKLines;
    }

    /**
     * 开始同步
     *
     * @param whenReceiveKLineData 当websocket收到消息时的回调，
     * @param taskExecutor         执行的线程池，回调也会通过这个线程池执行
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Set<String>> startSync(
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService taskExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            HashSet<String> set = new HashSet<>();

            T requestSender = startRegistryKLine();

            try {
                for (Pair<String, KLineInterval> kLine : listenKLines) {
                    String key = whenRegistryKLine(kLine.getKey(), kLine.getValue(), whenReceiveKLineData, taskExecutor, requestSender);

                    set.add(key);
                }

                endRegistryKLine(requestSender);
                return set;
            } catch (Exception e) {
                throw new RuntimeException("同步k线数据发生错误", e);
            }
        }, taskExecutor);
    }

    /**
     * 开始同步k显示数据
     */
    protected abstract T startRegistryKLine();

    /**
     * 注册监听k线时调用
     *
     * @param symbol               交易对
     * @param kLineInterval        k线频率
     * @param whenReceiveKLineData 收到k线数据的回调
     * @param taskExecutor         执行的线程池
     */
    protected abstract String whenRegistryKLine(
            String symbol,
            KLineInterval kLineInterval,
            SubscribeResultInvocationHandler whenReceiveKLineData,
            ExecutorService taskExecutor,
            T requestSender
    );

    /**
     * k线数据同步结束
     */
    protected abstract void endRegistryKLine(T requestSender);
}

