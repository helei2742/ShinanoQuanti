package com.helei.tradedatacenter.config;

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
        // 创建 Flink 流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .createLocalEnvironment();
//                .createRemoteEnvironment(jobManagerHost, jobManagerPort);
        // 可选的其他配置
        // env.setParallelism(4);  // 设置并行度
        return env;
    }
}
