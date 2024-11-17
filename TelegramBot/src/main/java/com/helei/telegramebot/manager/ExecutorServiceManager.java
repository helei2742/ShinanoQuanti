package com.helei.telegramebot.manager;


import com.helei.util.NamedThreadFactory;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Component
public class ExecutorServiceManager {

    public final ExecutorService commonExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("common处理线程池"));


    public final ExecutorService twitterExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("twitter处理线程池"));
}
