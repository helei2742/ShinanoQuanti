package com.helei.tradesignalprocess.support;


import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.cexapi.CEXApiFactory;
import com.helei.constants.KLineInterval;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.tradesignalprocess.stream.a_klinesource.HistoryKLineLoader;
import com.helei.tradesignalprocess.stream.a_datasource.MemoryKLineDataPublisher;
import com.helei.tradesignalprocess.stream.a_datasource.MemoryKLineSource;
import com.helei.dto.trade.KLine;
import com.helei.tradesignalprocess.util.KLineBuffer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KLineTradingDecision {
    private MemoryKLineDataPublisher dataPublisher;

    private MemoryKLineSource memoryKLineSource_btc_15m;
    private MemoryKLineSource memoryKLineSource_btc_2h;

    private MemoryKLineSource memoryKLineSource_eth;

    private String btcusdt = "btcusdt";

    private String ethusdt = "ethusdt";

    BinanceWSApiClient streamClient;
    BinanceWSApiClient normalClient;

    @BeforeAll
    public void before() {
        try {
            streamClient = CEXApiFactory.binanceApiClient("wss://stream.binance.com:9443/stream");
//            normalClient = CEXApiFactory.binanceApiClient(BinanceApiUrl.WS_NORMAL_URL);
//
//            CompletableFuture.allOf(streamClient.connect(), normalClient.connect()).get();

//            dataPublisher = new MemoryKLineDataPublisher(streamClient, normalClient, 100, 200, 3)
//                    .addListenKLine(btcusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.h_2, KLineInterval.m_15))
//                    .addListenKLine(ethusdt, Arrays.asList(KLineInterval.M_1, KLineInterval.d_1, KLineInterval.m_15));
//
//            memoryKLineSource_btc_2h = new MemoryKLineSource(btcusdt, KLineInterval.h_2, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
//            memoryKLineSource_btc_15m = new MemoryKLineSource(btcusdt, KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
//
//            memoryKLineSource_eth = new MemoryKLineSource(ethusdt, KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0), dataPublisher);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StreamExecutionEnvironment env;
    private StreamExecutionEnvironment env2;


    @Test
    public void testKLine() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
       streamClient.setProxy(new InetSocketAddress("127.0.0.1", 7890));
        streamClient.connect().get();
        streamClient.getStreamApi()
                .builder()
                .symbol("btcusdt")
                .addSubscribeEntity(
                        StreamSubscribeEntity
                                .builder()
                                .symbol("btcusdt")
                                .subscribeType(WebSocketStreamType.KLINE)
                                .invocationHandler(new SubscribeResultInvocationHandler() {
                                    @Override
                                    public void invoke(String streamName, JSONObject result) {
                                        System.out.println(result);
                                    }
                                })
                                .build()
                                .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, KLineInterval.m_15.getDescribe())
                )
                .subscribe();

               TimeUnit.MINUTES.sleep(30);
    }

    @SneakyThrows
    @Test
    public void testPST() {

        DataStreamSource<KLine> streamSource = env.addSource(memoryKLineSource_btc_2h).setParallelism(1);
        streamSource.print();

        env.execute("123");
        TimeUnit.MINUTES.sleep(100);
    }

    @Test
    public void testHistoryKLineLoader() throws InterruptedException {

        KLineBuffer kb = new KLineBuffer(10);

//        ArrayBlockingQueue<KLine> abq = new ArrayBlockingQueue<>(10);
        AtomicInteger counter = new AtomicInteger();

        new HistoryKLineLoader(200, normalClient, Executors.newVirtualThreadPerTaskExecutor())
                .startLoad("btcusdt", KLineInterval.m_15, LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli(), kLines -> {
                    System.out.println("get klines count " + kLines.size());
                    for (KLine kLine : kLines) {
                        try {
                            kb.put(kLine);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("add kline " + counter.incrementAndGet() + ", buffer size " + kb.size());
                    }
                }).thenRun(() -> {
                    System.out.println("end of history");
                });

        KLine aline = null;
        while (true) {

            aline = kb.take();
            System.out.println(aline);
            TimeUnit.SECONDS.sleep(1);
            log.info("get line [{}]", aline);
        }

//        TimeUnit.SECONDS.sleep(1000);
    }

}
