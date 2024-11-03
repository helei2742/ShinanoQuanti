package com.helei.tradesignalcenter.stream.a_klinesource;

import com.helei.constants.CEXType;
import com.helei.constants.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.trade.KLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

@Slf4j
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
        KafkaRealTimeSourceFactory sourceFactory = new KafkaRealTimeSourceFactory("btcusdt", Set.of(KLineInterval.h_1));
        KafkaConsumer<String, KLine> consumer = sourceFactory.loadRTKLineStream(CEXType.BINANCE, RunEnv.NORMAL,TradeType.CONTRACT);


        while (true) {
            ConsumerRecords<String, KLine> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, KLine> record : records) {

                log.info(record.value().toString());
            }
        }

    }
}
