package com.helei.tradesignalcenter.stream.a_datasource;

import com.helei.binanceapi.constants.BinanceApiUrl;
import com.helei.constants.KLineInterval;
import com.helei.dto.trade.KLine;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

class MemoryKLineSourceTest {

    private StreamExecutionEnvironment env;


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    public void test1() throws Exception {
        MemoryKLineSource kLineSource = new MemoryKLineSource(
                "BTCUSDT",
                List.of(KLineInterval.d_1, KLineInterval.h_2),
                LocalDateTime.of(2020, 1, 1, 1, 1).toInstant(ZoneOffset.UTC).toEpochMilli(),
                BinanceApiUrl.WS_SPOT_STREAM_URL,
                BinanceApiUrl.WS_NORMAL_URL,
                200
        );

        DataStreamSource<KLine> streamSource = env.addSource(kLineSource);

        KeyedStream<KLine, String> stream = streamSource.keyBy(KLine::getStreamKey);

        SingleOutputStreamOperator<KLine> process = stream.process(new KeyedProcessFunction<String, KLine, KLine>() {
            @Override
            public void processElement(KLine kLine, KeyedProcessFunction<String, KLine, KLine>.Context context, Collector<KLine> collector) throws Exception {
                if (kLine.getKLineInterval().equals(KLineInterval.h_2)) {
                    collector.collect(kLine);
                }
            }
        }).setParallelism(3);

        process.print();
        env.execute("test kline");
        TimeUnit.MINUTES.sleep(1000);
    }
}
