package com.helei.tradedatacenter.config;

import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlinkConfig {
    // 使用远程集群连接
    String jobManagerHost = "192.168.1.2"; // JobManager 主机地址
    int jobManagerPort = 8081; // JobManager REST 端口（通常为 8081）

    @Bean(name = "flinkEnv")
    public StreamExecutionEnvironment streamExecutionEnvironment() {
        // 创建 Flink 配置对象
        org.apache.flink.configuration.Configuration config = new org.apache.flink.configuration.Configuration();

        // 设置 taskmanager.memory.network.fraction, 比例设为 15%
        config.setDouble(String.valueOf(TaskManagerOptions.NETWORK_MEMORY_FRACTION), 0.15);

        // 设置固定的网络内存大小，例如 128 MB
        config.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.ofMebiBytes(128));


        // 创建 Flink 流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .createLocalEnvironment(config);
//                .createRemoteEnvironment(jobManagerHost, jobManagerPort);
        // 可选的其他配置
        // env.setParallelism(4);  // 设置并行度
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        return env;
    }
    @Bean(name = "flinkEnv2")
    public StreamExecutionEnvironment streamExecutionEnvironment2() {
        // 创建 Flink 配置对象
        org.apache.flink.configuration.Configuration config = new org.apache.flink.configuration.Configuration();

        // 设置 taskmanager.memory.network.fraction, 比例设为 15%
        config.setDouble(String.valueOf(TaskManagerOptions.NETWORK_MEMORY_FRACTION), 0.15);

        // 设置固定的网络内存大小，例如 128 MB
        config.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.ofMebiBytes(128));


        // 创建 Flink 流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .createLocalEnvironment(config);
//                .createRemoteEnvironment(jobManagerHost, jobManagerPort);
        // 可选的其他配置
        // env.setParallelism(4);  // 设置并行度
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        return env;
    }
}
