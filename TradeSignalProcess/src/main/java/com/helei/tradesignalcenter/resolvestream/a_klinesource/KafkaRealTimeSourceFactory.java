package com.helei.tradesignalcenter.resolvestream.a_klinesource;

import com.helei.constants.CEXType;
import com.helei.constants.KLineInterval;
import com.helei.constants.TradeType;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.config.TradeSignalConfig;
import com.helei.tradesignalcenter.serialization.KafkaKLineSchema;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.flink.kafka.shaded.org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;


/**
 * kafka实时数据源
 * <p>
 * 1.获取实时k线数据，一个KafkaRealTimeKLineSource对象代表一种k线（相同的symbol和interval）
 * </p>
 */
@Slf4j
public class KafkaRealTimeSourceFactory {


    /**
     * 交易对
     */
    private final String symbol;

    /**
     * k线频率
     */
    private final Set<KLineInterval> intervals;

    /**
     * 实时kafka设置
     */
    private final TradeSignalConfig.RealtimeKafkaConfig realtimeKafkaConfig;


    public KafkaRealTimeSourceFactory(
            String symbol,
            Set<KLineInterval> intervals
    ) {
        this.symbol = symbol;
        this.intervals = intervals;
        realtimeKafkaConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG.getRealtime().getKafka();
    }

    /**
     * 创建实时k线数据源
     *
     * @param cexType cexType
     * @param tradeType tradeType
     */
    public KafkaConsumer<String, KLine> loadRTKLineStream(
            CEXType cexType,
            TradeType tradeType
    ) {
        List<String> topicList = intervals
                .stream()
                .map(interval -> KafkaUtil.resolveKafkaTopic(cexType, KafkaUtil.getKLineStreamName(symbol, interval), tradeType))
                .toList();

        String bootstrapServer = realtimeKafkaConfig.getBootstrapServer();
        String groupId = realtimeKafkaConfig.getGroupId();

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "com.helei.tradesignalcenter.serialization.KafkaKLineSchema");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "com.helei.tradesignalcenter.serialization.KafkaKLineSchema");

        KafkaConsumer<String, KLine> consumer = null;
        try {
            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(topicList);
            log.info("开始创建kafka kline数据流，kafka server[{}], groupId[{}], topics[{}] ",
                    bootstrapServer, groupId, topicList);
        } catch (Exception e) {
            log.error("创建kafka consumer失败,kafka server[{}], groupId[{}]", bootstrapServer, groupId);
            throw new RuntimeException("创建kafka consumer失败", e);
        }

        return consumer;
    }



}
