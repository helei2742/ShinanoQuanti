package com.helei.tradesignalprocess.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.indicator.Indicator;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
        import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class IndicatorMapSerializer extends Serializer<IndicatorMap> {

    @Override
    public void write(Kryo kryo, Output output, IndicatorMap indicatorMap) {

        int size = indicatorMap.getMap().entrySet().size();
        log.debug("序列化 [{}]  size [{}]", indicatorMap, size);
        output.writeInt(size );
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        ) {
            for (Map.Entry<String, Indicator> entry : indicatorMap.getMap().entrySet()) {
                objectOutputStream.writeObject(entry.getKey());
                objectOutputStream.writeObject(entry.getValue());
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            output.writeInt(byteArray.length);
            output.writeBytes(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IndicatorMap read(Kryo kryo, Input source, Class<IndicatorMap> aClass) {
        IndicatorMap indicatorMap = new IndicatorMap();

        int size = source.readInt();
        int len = source.readInt();
        byte[] bytes = new byte[len];
        source.readBytes(bytes);

        ConcurrentHashMap<String, Indicator> indicators = indicatorMap.getMap();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            for (int i = 0; i < size; i++) {
                try {
                    String key = (String) objectInputStream.readObject();
                    Indicator value = (Indicator) objectInputStream.readObject();
                    indicators.put(key, value);
                }catch (EOFException e) {
                    log.error("反序列化出错， 已序列化出的结果:[{}]", indicatorMap, e);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            log.error("反序列化出错， 已序列化出的结果:[{}]", indicatorMap, e);
            throw new RuntimeException(e);
        }

        log.debug("反序列化得到 [{}]", indicatorMap);
        return indicatorMap;
    }
}
