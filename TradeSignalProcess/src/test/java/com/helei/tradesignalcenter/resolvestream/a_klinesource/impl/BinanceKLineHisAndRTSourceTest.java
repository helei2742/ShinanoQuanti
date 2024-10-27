package com.helei.tradesignalcenter.resolvestream.a_klinesource.impl;

import cn.hutool.core.util.BooleanUtil;
import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import com.helei.tradesignalcenter.config.FlinkConfig;
import com.helei.tradesignalcenter.constants.KLineTypeInfo;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class BinanceKLineHisAndRTSourceTest {

    private static StreamExecutionEnvironment env;

    private static String symbol = "btcusdt";


    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        env = FlinkConfig.streamExecutionEnvironment();
    }

    @Test
    public void testHisAndReTSource() throws Exception {
        BinanceKLineHisAndRTSource source = new BinanceKLineHisAndRTSource(
                symbol,
                Set.of(KLineInterval.m_1),
                LocalDateTime.of(2024, 10, 27, 21, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        );

        DataStream<KLine> stream = env.addSource(source).returns(new KLineTypeInfo());

        stream.process(new ProcessFunction<KLine, Object>() {
            @Override
            public void open(OpenContext openContext) throws Exception {
                super.open(openContext);
            }

            @Override
            public void processElement(KLine kLine, ProcessFunction<KLine, Object>.Context context, Collector<Object> collector) throws Exception {
//                if (BooleanUtil.isFalse(kLine.isEnd())) {
                    System.out.println(kLine);
//                }
            }
        }).setParallelism(1);

        env.execute();
        TimeUnit.MINUTES.sleep(300);
    }
}
