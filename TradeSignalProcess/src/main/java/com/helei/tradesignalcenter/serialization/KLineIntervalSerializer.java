package com.helei.tradesignalcenter.serialization;

import com.helei.constants.KLineInterval;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

public class KLineIntervalSerializer extends TypeSerializer<KLineInterval> {

    @Override
    public boolean isImmutableType() {
        return false;
    }

    @Override
    public TypeSerializer<KLineInterval> duplicate() {
        return null;
    }

    @Override
    public KLineInterval createInstance() {
        return null;
    }

    @Override
    public KLineInterval copy(KLineInterval interval) {
        return null;
    }

    @Override
    public KLineInterval copy(KLineInterval interval, KLineInterval t1) {
        return null;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void serialize(KLineInterval interval, DataOutputView dataOutputView) throws IOException {

    }

    @Override
    public KLineInterval deserialize(DataInputView dataInputView) throws IOException {
        return null;
    }

    @Override
    public KLineInterval deserialize(KLineInterval interval, DataInputView dataInputView) throws IOException {
        return null;
    }

    @Override
    public void copy(DataInputView dataInputView, DataOutputView dataOutputView) throws IOException {

    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public TypeSerializerSnapshot<KLineInterval> snapshotConfiguration() {
        return null;
    }
}
