package com.helei.tradesignalcenter.stream.e_order;

import com.helei.tradesignalcenter.dto.OriginOrder;
import com.helei.util.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;


@Slf4j
public class KafkaOriginOrderCommitter extends AbstractOrderCommitter<OriginOrder>{


    @Override
    public Sink<OriginOrder> getCommitSink() {
        String bootstrap = tradeSignalConfig.getRealtime().getKafka().getBootstrapServer();
        String topic = tradeSignalConfig.getSinkTopic();

        log.info("创建 原始订单 Kafka Sink [{}] - topic [{}]", bootstrap, topic);

        return KafkaSink.<OriginOrder>builder()
                .setBootstrapServers(bootstrap)
                .setRecordSerializer(
                        KafkaRecordSerializationSchema
                                .builder()
                                .setTopic(topic)
                                .setValueSerializationSchema(new KafkaOriginOrderSchema())
                                .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE).build();
    }


    static class KafkaOriginOrderSchema implements SerializationSchema<OriginOrder> {
        @Override
        public byte[] serialize(OriginOrder originOrder) {
            return Serializer.Algorithm.Protostuff.serialize(originOrder);
        }
    }
}
