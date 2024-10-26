package com.helei.tradesignalcenter.resolvestream.a_klinesource;

import com.helei.constants.CEXType;
import com.helei.constants.KLineInterval;
import com.helei.constants.TradeType;
import com.helei.tradesignalcenter.config.TradeSignalConfig;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


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
    private final KLineInterval interval;

    /**
     * 实时kafka设置
     */
    private final TradeSignalConfig.RealtimeKafkaConfig realtimeKafkaConfig;


    public KafkaRealTimeSourceFactory(String symbol, KLineInterval interval) {
        this.symbol = symbol;
        this.interval = interval;
        realtimeKafkaConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG.getKafka();
    }


    /**
     * 创建实时k线数据源
     *
     * @param env     env
     * @param cexType cexType
     * @param tradeType tradeType
     * @return DataStream<String>
     */
    public DataStream<String> buildRTKLineStream(
            StreamExecutionEnvironment env,
            CEXType cexType,
            TradeType tradeType
    ) {
        String streamName = KafkaUtil.getKLineStreamName(symbol, interval);
        String kafkaTopic = KafkaUtil.resolveKafkaTopic(cexType, streamName, tradeType);

        String bootstrapServer = realtimeKafkaConfig.getBootstrapServer();

        String groupId = realtimeKafkaConfig.getGroupId();

        log.info("开始创建kafka topic[{}] kline数据流，kafka server[{}], groupId[{}]",
                kafkaTopic, bootstrapServer, groupId);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(kafkaTopic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        return env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source[" + kafkaTopic + "]");
    }

}
