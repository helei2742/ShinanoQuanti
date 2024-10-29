package com.helei.tradesignalcenter.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.helei.constants.KLineInterval;
import com.helei.dto.IndicatorMap;
import com.helei.dto.KLine;

public class KLineSerializer extends Serializer<KLine> {


    @Override
    public void write(Kryo kryo, Output output, KLine kLine) {
        output.writeString(kLine.getSymbol());
        output.writeDouble(kLine.getOpen());
        output.writeDouble(kLine.getClose());
        output.writeDouble(kLine.getHigh());
        output.writeDouble(kLine.getLow());
        output.writeDouble(kLine.getVolume());
        output.writeLong(kLine.getOpenTime());
        output.writeLong(kLine.getCloseTime());
        output.writeBoolean(kLine.isEnd());

        Serializer klineIntervalSerializer = kryo.getSerializer(KLineInterval.class);
        Serializer indicatorMapSerializer = kryo.getSerializer(IndicatorMap.class);

        klineIntervalSerializer.write(kryo, output, kLine.getKLineInterval());

        indicatorMapSerializer.write(kryo, output, kLine.getIndicators());
    }

    @Override
    public KLine read(Kryo kryo, Input input, Class<KLine> aClass) {
        String symbol = input.readString();
        double open = input.readDouble();
        double close = input.readDouble();
        double high = input.readDouble();
        double low = input.readDouble();
        double volume = input.readDouble();
        long openTime = input.readLong();
        long closeTime = input.readLong();
        boolean end = input.readBoolean();

        Serializer klineIntervalSerializer = kryo.getSerializer(KLineInterval.class);
        Serializer indicatorMapSerializer = kryo.getSerializer(IndicatorMap.class);


        KLineInterval kLineInterval = (KLineInterval) klineIntervalSerializer.read(kryo, input, KLineInterval.class);
        IndicatorMap indicatorMap = (IndicatorMap) indicatorMapSerializer.read(kryo, input, IndicatorMap.class);


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
                .kLineInterval(kLineInterval)
                .indicators(indicatorMap)
                .build();
    }
}
