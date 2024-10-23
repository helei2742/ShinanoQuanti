package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.binanceapi.constants.BinanceApiUrl;
import com.helei.constants.KLineInterval;
import com.helei.dto.KLine;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemoryKLineSourceTest {

    @Autowired
    @Qualifier("flinkEnv")
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
                List.of(KLineInterval.h_1),
                LocalDateTime.of(2020, 1, 1, 1, 1).toInstant(ZoneOffset.UTC).toEpochMilli(),
                BinanceApiUrl.WS_SPOT_STREAM_URL,
                BinanceApiUrl.WS_NORMAL_URL,
                200
        );

        DataStreamSource<KLine> streamSource = env.addSource(kLineSource).setParallelism(1);;

        streamSource.print().setParallelism(1);;

        env.execute("test kline");
        TimeUnit.MINUTES.sleep(1000);
    }
}
