package com.helei.tradesignalcenter.serialization;

import com.alibaba.fastjson.JSONObject;
import com.helei.dto.trade.KLine;
import com.helei.binanceapi.supporter.KLineMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.kafka.shaded.org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KafkaKLineSchema implements DeserializationSchema<KLine>,
        SerializationSchema<KLine>, Deserializer<KLine> {
    @Override
    public KLine deserialize(byte[] bytes) throws IOException {
        JSONObject jb = JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8));
        return KLineMapper.mapJsonToKLine(jb);
    }

    @Override
    public boolean isEndOfStream(KLine s) {
        return s == KLine.STREAM_END_KLINE;
    }

    @Override
    public byte[] serialize(KLine kline) {
        return JSONObject.toJSONString(kline).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public TypeInformation<KLine> getProducedType() {
        return TypeInformation.of(KLine.class);
    }

    @Override
    public KLine deserialize(String s, byte[] bytes) {
        try {
            return deserialize(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
