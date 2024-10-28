package com.helei.tradesignalcenter.serialization;

import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import com.helei.dto.indicator.Indicator;
import com.helei.dto.indicator.MA;
import com.helei.dto.indicator.config.IndicatorConfig;
import com.helei.dto.indicator.config.MAConfig;
import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataInputViewStreamWrapper;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.core.memory.DataOutputViewStreamWrapper;
import org.apache.flink.runtime.state.JavaSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KLineSerializer extends TypeSerializer<KLine> {

    @Override
    public boolean isImmutableType() {
        return false;
    }

    @Override
    public KLine createInstance() {
        return new KLine();
    }

    @Override
    public KLine copy(KLine from) {
        return from.clone(); // 假设 KLine 实现了深拷贝的 clone() 方法
    }

    @Override
    public KLine copy(KLine from, KLine reuse) {
        return copy(from);
    }

    @Override
    public int getLength() {
        return -1; // 返回 -1 表示对象大小可变
    }

    @Override
    public void serialize(KLine kLine, DataOutputView target) throws IOException {
        target.writeUTF(kLine.getSymbol() == null ? "" : kLine.getSymbol());
        target.writeDouble(kLine.getOpen());
        target.writeDouble(kLine.getClose());
        target.writeDouble(kLine.getHigh());
        target.writeDouble(kLine.getLow());
        target.writeDouble(kLine.getVolume());
        target.writeLong(kLine.getOpenTime());
        target.writeLong(kLine.getCloseTime());
        target.writeBoolean(kLine.isEnd());
//        target.writeUTF(kLine.getKLineInterval().getDescribe());

//        int size = kLine.getIndicators().size();
//        target.writeInt(size);
//
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//        ) {
//            for (Map.Entry<IndicatorConfig<? extends Indicator>, Indicator> entry : kLine.getIndicators().entrySet()) {
//                objectOutputStream.writeObject(entry.getKey());
//                objectOutputStream.writeObject(entry.getValue());
//                objectOutputStream.flush();
//            }
//            byte[] byteArray = byteArrayOutputStream.toByteArray();
//            target.writeUTF(Base64.getEncoder().encodeToString(byteArray));
//        }
    }

    @Override
    public KLine deserialize(DataInputView source) throws IOException {
        String symbol = source.readUTF();
        double open = source.readDouble();
        double close = source.readDouble();
        double high = source.readDouble();
        double low = source.readDouble();
        double volume = source.readDouble();
        long openTime = source.readLong();
        long closeTime = source.readLong();
        boolean end = source.readBoolean();

        KLineInterval kLineInterval = KLineInterval.STATUS_MAP.get(source.readUTF());
//        int size = source.readInt();
//        String base64Str = source.readUTF();
//        byte[] bytes = Base64.getDecoder().decode(base64Str);
//
//        HashMap<IndicatorConfig<? extends Indicator>, Indicator> indicators = new HashMap<>();
//        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
//            for (int i = 0; i < size; i++) {
//                IndicatorConfig<? extends Indicator> key = (IndicatorConfig<? extends Indicator>) objectInputStream.readObject();
//                Indicator value = (Indicator) objectInputStream.readObject();
//                indicators.put(key, value);
//            }
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }


        return KLine.builder()
                .symbol(symbol)
                .open(open)
                .close(close)
                .high(high)
                .low(low)
                .volume(volume)
                .openTime(openTime)
                .closeTime(closeTime)
                .end(end)
//                .kLineInterval(kLineInterval)
//                .indicators(indicators)
                .build();
    }

    @Override
    public TypeSerializer<KLine> duplicate() {
        return this;
    }

    @Override
    public KLine deserialize(KLine reuse, DataInputView source) throws IOException {
        return deserialize(source);
    }

    @Override
    public void copy(DataInputView source, DataOutputView target) throws IOException {
        target.write(source, getLength());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KLineSerializer;
    }

    @Override
    public int hashCode() {
        return KLine.class.hashCode();
    }

    @Override
    public TypeSerializerSnapshot<KLine> snapshotConfiguration() {
        return new JavaSerializer.JavaSerializerSnapshot<>();
    }

    public static void main(String[] args) throws IOException {
//        HashMap<IndicatorConfig<? extends Indicator>, Indicator> indicators = new HashMap<>();
//        MAConfig maConfig = new MAConfig(12);
//        indicators.put(maConfig, new MA(109.8));
//
//
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//
//        ) {
//            int size = indicators.size();
//            for (Map.Entry<IndicatorConfig<? extends Indicator>, Indicator> entry : indicators.entrySet()) {
//                objectOutputStream.writeObject(entry.getKey());
//                objectOutputStream.writeObject(entry.getValue());
//                objectOutputStream.flush();
//            }
//
//            byte[] byteArray = byteArrayOutputStream.toByteArray();
//
//            String s = Base64.getEncoder().encodeToString(byteArray);
//
//            byte[] decode = Base64.getDecoder().decode(s);
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decode);
//            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//
//
//            HashMap<IndicatorConfig<? extends Indicator>, Indicator> test = new HashMap<>();
//            for (int i = 0; i < size; i++) {
//                IndicatorConfig<? extends Indicator> key = (IndicatorConfig<? extends Indicator>) objectInputStream.readObject();
//                Indicator value = (Indicator) objectInputStream.readObject();
//                test.put(key, value);
//            }
//
//            test.entrySet().forEach(e->{
//                System.out.println(((MAConfig) e.getKey()));
//                System.out.println(e.getValue().toString());
//            });
//
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//
//        MAConfig maConfig = new MAConfig(12);
//        KLine build = KLine.builder().build();
//        build.setKLineInterval(KLineInterval.d_1);
//        build.setIndicators(new HashMap<>());
//        build.getIndicators().put(maConfig, new MA(123.2));
//
//        // 创建一个 DataOutputView，写入序列化数据
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        DataOutputView outputView = new DataOutputViewStreamWrapper(byteArrayOutputStream);
//
//        KLineSerializer serializer = new KLineSerializer();
//        // 序列化对象
//        serializer.serialize(build, outputView);
//
//        // 将序列化数据输入到 DataInputView 以反序列化
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//        DataInputView inputView = new DataInputViewStreamWrapper(byteArrayInputStream);
//
//        // 反序列化对象
//        KLine deserializedKLine = serializer.deserialize(inputView);
//
//        // 验证原对象和反序列化对象是否相等
//        System.out.println(deserializedKLine);
    }
}
