package com.helei.tradesignalcenter.serialization;

import org.apache.flink.kafka.shaded.org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KafkaStringDeserializer implements Deserializer<String> {

    @Override
    public String deserialize(String s, byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
