package com.helei.twiterstream;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

@Slf4j
public class RetryConnector {

    private final int retryLimit;

    private final AtomicInteger tryTimes;

    private final int timeWindowSecond;

    private final ExecutorService executor;

    public RetryConnector(
            int retryLimit,
            int timeWindowSecond,

            ExecutorService executor
    ) {
        this.retryLimit = retryLimit;
        this.timeWindowSecond = timeWindowSecond;
        this.executor = executor;

        this.tryTimes = new AtomicInteger(0);
    }

    /**
     * 重连
     *
     * @param connectMethod   执行的方法
     * @param startPrint      开始时打印的字符串
     * @param retryPrint      重试时打印的字符串
     * @param finalErrorPrint 失败打印的字符串
     * @return 是否成功
     */
    public boolean retryConnect(
            BiFunction<Integer, Integer, Boolean> connectMethod,
            String startPrint,
            String retryPrint,
            String finalErrorPrint
    ) {
        Exception lastException = null;

        int i = 0;
        while ((i = tryTimes.incrementAndGet()) <= retryLimit) {
            try {
                log.info("[{}/{}] - {}", i, retryLimit, startPrint);
                if (connectMethod.apply(i, retryLimit)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("[{}/{}] - {}, 准备重试. errorMsg[{}]", i, retryLimit, retryPrint, e.getMessage());
                setDecretTimer();
                lastException = e;
            }
        }

        log.error("[{}/{}] - {}", retryLimit, retryLimit, finalErrorPrint);
        throw new RuntimeException(finalErrorPrint, lastException);
    }


    private void setDecretTimer() {
        executor.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(timeWindowSecond);
                tryTimes.decrementAndGet();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
