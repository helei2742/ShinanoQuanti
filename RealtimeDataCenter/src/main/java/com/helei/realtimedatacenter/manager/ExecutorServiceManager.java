package com.helei.realtimedatacenter.manager;

import com.helei.util.NamedThreadFactory;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Component
public class ExecutorServiceManager {

    /**
     * 执行连接的线程池
     */
    private final ExecutorService connectExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("连接线程池"));

    /**
     * 同步任务执行的线程池
     */
    private final ExecutorService syncTaskExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("同步任务线程池"));

    /**
     * 处理账户事件的线程池
     */
    private final ExecutorService accountEventExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("账户事件处理线程池"));

    /**
     * 获取账户实时信息推送的线程池
     */
    private final ExecutorService accountRTDataExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("账户实时数据流获取线程池"));

    /**
     * k线加载任务处理的线程池
     */
    private final ExecutorService klineTaskExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("k线数据加载线程池"));
}

