package com.helei.tradesignalcenter.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.helei.dto.IndicatorMap;
import com.helei.dto.indicator.Indicator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class IndicatorMapSerializer extends Serializer<IndicatorMap> {

    @Override
    public void write(Kryo kryo, Output output, IndicatorMap indicatorMap) {
        output.writeInt(indicatorMap.getMap().size());
        for (Map.Entry<String, Indicator> entry : indicatorMap.getMap().entrySet()) {
            kryo.writeClassAndObject(output, entry.getKey());
            kryo.writeClassAndObject(output, entry.getValue());
        }
    }

    @Override
    public IndicatorMap read(Kryo kryo, Input input, Class<IndicatorMap> aClass) {
        int size = input.readInt();
        IndicatorMap indicatorMap = new IndicatorMap();
        HashMap<String, Indicator> indicators = indicatorMap.getMap();
        for (int i = 0; i < size; i++) {
            String key = (String) kryo.readClassAndObject(input);
            Indicator value = (Indicator) kryo.readClassAndObject(input);
            indicators.put(key, value);
        }
        return indicatorMap;
    }
//    @Override
//    public void write(Kryo kryo, Output output, IndicatorMap indicatorMap) {
//        int size = indicatorMap.getMap().size();
//        output.writeInt(size);
//
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//        ) {
//            for (Map.Entry<String, Indicator> entry : indicatorMap.getMap().entrySet()) {
//                objectOutputStream.writeObject(entry.getKey());
//                objectOutputStream.writeObject(entry.getValue());
//                objectOutputStream.flush();
//            }
//            byte[] byteArray = byteArrayOutputStream.toByteArray();
//            output.writeInt(byteArray.length);
//            output.writeBytes(byteArray);
//            output.flush();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public IndicatorMap read(Kryo kryo, Input source, Class<IndicatorMap> aClass) {
//        int size = source.readInt();
//        int len = source.readInt();
//        byte[] bytes = new byte[len];
//        source.readBytes(bytes);
//        IndicatorMap indicatorMap = new IndicatorMap();
//        HashMap<String, Indicator> indicators = indicatorMap.getMap();
//        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
//            for (int i = 0; i < size; i++) {
//                    String key = (String) objectInputStream.readObject();
//                    Indicator value = (Indicator) objectInputStream.readObject();
//                    indicators.put(key, value);
//
//            }
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return indicatorMap;
//    }
}
