package com.helei;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.trade.TradeType;
import com.helei.realtimedatacenter.config.RealtimeConfig;
import com.helei.util.KafkaUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoadKafkaKLineData2File {

    private final RealtimeConfig realtimeConfig = RealtimeConfig.INSTANCE;

    private final String dirPath = "/Users/helei/develop/ideaworkspace/ShinanoQuanti/kline";

    private final String symbol = "btcusdt";

    private final KLineInterval kLineInterval = KLineInterval.m_1;

    private final RunEnv runEnv = RunEnv.TEST_NET;

    private final TradeType tradeType = TradeType.CONTRACT;

    private final CEXType cexType = CEXType.BINANCE;


    @Test
    public void load() throws IOException, InterruptedException {
        String topic = KafkaUtil.resolveKafkaTopic(cexType, KafkaUtil.getKLineStreamName(symbol, kLineInterval), runEnv, tradeType);
        String filePath = dirPath + File.separator + symbol + File.separator + kLineInterval;
        File file = new File(filePath);

        if (!file.exists()) {
            Files.createDirectories(file.toPath());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file, topic + ".dat")))) {
            ConcurrentKafkaListenerContainerFactory<String, String> factory = kafkaListenerContainerFactory();
            ConcurrentMessageListenerContainer<String, String> container = factory.createContainer(topic);
            container.setupMessageListener((MessageListener<String, String>) record -> {
                System.out.println(record.value());
                try {
                    writer.write(record.value());
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            container.start();
            TimeUnit.MINUTES.sleep(100);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> kafkaConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, realtimeConfig.getKafka().getBootstrap_servers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "loader");  // 消费者组ID
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return configProps;
    }

    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaConfigs());
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
