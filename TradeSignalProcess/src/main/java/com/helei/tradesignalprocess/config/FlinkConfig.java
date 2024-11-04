package com.helei.tradesignalprocess.config;

import org.apache.flink.configuration.*;
        import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.net.URL;


public class FlinkConfig {

    private static final String CONFIG_FILE = "flink-conf";

    public static StreamExecutionEnvironment streamExecutionEnvironment() {
        // 创建 Flink 配置对象
        URL url = TradeSignalConfig.class.getClassLoader().getResource(CONFIG_FILE);

        if (url == null) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE);
        }

        Configuration config = GlobalConfiguration.loadConfiguration(url.getPath());
        // 设置 taskmanager.memory.network.fraction, 比例设为 15%
        config.setDouble(String.valueOf(TaskManagerOptions.NETWORK_MEMORY_FRACTION), 0.15);

        // 设置固定的网络内存大小，例如 128 MB
        config.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.ofMebiBytes(1024));
        return StreamExecutionEnvironment
                .createLocalEnvironment(config);
    }

    public StreamExecutionEnvironment streamExecutionEnvironment2() {
        // 创建 Flink 配置对象
        org.apache.flink.configuration.Configuration config = new org.apache.flink.configuration.Configuration();

        // 设置 taskmanager.memory.network.fraction, 比例设为 15%
        config.setDouble(String.valueOf(TaskManagerOptions.NETWORK_MEMORY_FRACTION), 0.15);

        // 设置固定的网络内存大小，例如 128 MB
        config.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.ofMebiBytes(1024));


        // 创建 Flink 流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .createLocalEnvironment(config);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
//                .createRemoteEnvironment(jobManagerHost, jobManagerPort);
        // 可选的其他配置
        // env.setParallelism(4);  // 设置并行度
        return env;
    }
}
