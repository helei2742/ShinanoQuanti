package com.helei.tradesignalcenter.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

public class KLineIntervalSerializer extends Serializer<KLineInterval> {

    @Override
    public void write(Kryo kryo, Output output, KLineInterval kLineInterval) {
        output.writeString(kLineInterval.getDescribe());
    }

    @Override
    public KLineInterval read(Kryo kryo, Input input, Class<KLineInterval> aClass) {
        return KLineInterval.STATUS_MAP.get(input.readString());
    }
}
