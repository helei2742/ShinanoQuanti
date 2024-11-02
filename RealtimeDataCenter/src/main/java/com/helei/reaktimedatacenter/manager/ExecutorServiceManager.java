package com.helei.reaktimedatacenter.manager;

import com.helei.util.NamedThreadFactory;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Component
public class ExecutorServiceManager {

    /**
     * 同步任务执行的线程池
     */
    private final ExecutorService syncTaskExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("同步任务线程池"));


    /**
     * 处理事件的线程池
     */
    private final ExecutorService eventExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("事件处理线程池"));

}
