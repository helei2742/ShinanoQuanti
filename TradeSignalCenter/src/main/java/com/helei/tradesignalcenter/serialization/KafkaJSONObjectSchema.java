package com.helei.tradesignalcenter.serialization;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KafkaJSONObjectSchema implements DeserializationSchema<JSONObject>, SerializationSchema<JSONObject> {
    @Override
    public JSONObject deserialize(byte[] bytes) throws IOException {
        return JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public boolean isEndOfStream(JSONObject s) {
        return s == null;
    }

    @Override
    public byte[] serialize(JSONObject s) {
        return s.toJSONString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public TypeInformation<JSONObject> getProducedType() {
        return TypeInformation.of(JSONObject.class);
    }
}
