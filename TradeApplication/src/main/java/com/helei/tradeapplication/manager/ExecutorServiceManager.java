package com.helei.tradeapplication.manager;


import com.helei.util.NamedThreadFactory;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Component
public class ExecutorServiceManager {

    private final ExecutorService tradeSignalResolveExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("kafka交易信号处理线程池"));


    private final ExecutorService connectExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("连接用线程池"));

    private final ExecutorService tradeOrderResolveExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("kafka订单处理线程池"));


    private final ExecutorService queryExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("查询用线程池"));


    private final ExecutorService tradeExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("交易用线程池"));


    private final ExecutorService orderExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("订单用线程池"));
}
