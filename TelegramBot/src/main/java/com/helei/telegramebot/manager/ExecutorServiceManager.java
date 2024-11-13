package com.helei.telegramebot.manager;


import com.helei.util.NamedThreadFactory;
import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class ExecutorServiceManager {

    public final ExecutorService commonExecutor = Executors.newThreadPerTaskExecutor(new NamedThreadFactory("common处理线程池"));

}
