package com.helei.tradesignalprocess.stream.e_trade_signal;

import com.helei.dto.kafka.TradeSignalTopic;
import com.helei.tradesignalprocess.config.TradeSignalConfig;
import com.helei.dto.trade.TradeSignal;
import com.helei.util.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;


@Slf4j
public class KafkaTradeSignalCommitter extends AbstractTradeSignalCommitter<TradeSignal> {


    @Override
    public Sink<TradeSignal> getCommitSink() {
        TradeSignalConfig.KafkaServerConfig kafkaServerConfig = tradeSignalConfig.getRealtime().getKafka().getOutput();
        String bootstrap = kafkaServerConfig.getBootstrapServer();
        TradeSignalTopic topic = tradeSignalConfig.getSinkTopic();

        log.info("创建 原始订单 Kafka Sink [{}] - topic [{}]", bootstrap, topic);

        Properties props = new Properties();
        props.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, kafkaServerConfig.getTransaction_timeout_ms());

        return KafkaSink.<TradeSignal>builder()
                .setBootstrapServers(bootstrap)
                .setRecordSerializer(
                        KafkaRecordSerializationSchema
                                .builder()
                                .setTopic(topic.toString())
                                .setValueSerializationSchema(new KafkaOriginOrderSchema())
                                .build()
                )
//                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setKafkaProducerConfig(props)
                .build();
    }


    static class KafkaOriginOrderSchema implements SerializationSchema<TradeSignal> {
        @Override
        public byte[] serialize(TradeSignal tradeSignal) {
            return Serializer.Algorithm.JSON.serialize(tradeSignal);
        }
    }
}
