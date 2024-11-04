package com.helei.tradesignalprocess.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.helei.constants.KLineInterval;

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
