package com.helei.tradesignalcenter.resolvestream.a_klinesource;

import com.helei.constants.CEXType;
import com.helei.constants.KLineInterval;
import com.helei.constants.TradeType;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class KafkaRealTimeSourceFactoryTest {

    private static StreamExecutionEnvironment env;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        // 创建 Flink 配置对象
        org.apache.flink.configuration.Configuration config = new org.apache.flink.configuration.Configuration();

        // 设置 taskmanager.memory.network.fraction, 比例设为 15%
        config.setDouble(String.valueOf(TaskManagerOptions.NETWORK_MEMORY_FRACTION), 0.15);

        // 设置固定的网络内存大小，例如 128 MB
        config.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.ofMebiBytes(1024));

        config.setString("classloader.check-leaked-classloader", "false");

        // 创建 Flink 流执行环境
        env = StreamExecutionEnvironment
                .createLocalEnvironment(config);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
    }

    @Test
    void buildRTKLineStream() throws Exception {
        KafkaRealTimeSourceFactory sourceFactory = new KafkaRealTimeSourceFactory("btcusdt", KLineInterval.h_1);
        DataStream<String> kLineStream = sourceFactory.buildRTKLineStream(env, CEXType.BINANCE, TradeType.CONTRACT);

        kLineStream.print();

        env.execute();
        TimeUnit.MINUTES.sleep(1000);
    }
}
