package com.helei.tradedatacenter.datasource;


import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.api.ws.BinanceWSStreamApi;
import com.helei.constants.KLineInterval;
import com.helei.binanceapi.constants.WebSocketStreamParamKey;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.util.KLineBuffer;
import com.helei.util.CustomBlockingQueue;
import lombok.extern.slf4j.Slf4j;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;


/**
 * 订阅k线，分发数据。
 * 1。创建类时自动链接Binance
 * 2.调用addListenKLine会向Binance api请求k线数据推送
 * 3.需要k线数据需要调用registry() 方法注册订阅
 * 最后，2步骤中得到推送的k线数据后，会遍历订阅者进行发送
 */
@Slf4j
public class MemoryKLineDataPublisher implements KLineDataPublisher {

    private final ExecutorService publishExecutor;

    /**
     * 获取流实时数据
     */
    private final BinanceWSStreamApi.StreamCommandBuilder streamCommandBuilder;

    /**
     * 普通api 用于获取历史数据
     */
    private final HistoryKLineLoader historyKLineLoader;

    /**
     * 实时k线数据buffer
     */
    private final ConcurrentMap<String, CustomBlockingQueue<KLine>> realTimeKLineBufferMap = new ConcurrentHashMap<>();

    private final int bufferSize;

    /**
     * 加载发布k线数据
     *
     * @param streamClient     streamClient
     * @param normalClient     normalClient
     * @param bufferSize       实时数据缓存区大小
     * @param historyLoadBatch 历史数据一次拉取的批大小
     * @param executorSize     处理线程池大小，由于registry()返回的KLineBuffer()是有界的，长时间不消费可能会导致处理的线程池县城被长期阻塞。
     */
    public MemoryKLineDataPublisher(
            BinanceWSApiClient streamClient,
            BinanceWSApiClient normalClient,
            int bufferSize,
            int historyLoadBatch,
            int executorSize
    ) {
        this.publishExecutor = Executors.newFixedThreadPool(executorSize);

        this.historyKLineLoader = new HistoryKLineLoader(historyLoadBatch, normalClient, publishExecutor);
        streamCommandBuilder = streamClient
                .getStreamApi()
                .builder();

        this.bufferSize = bufferSize;
    }

    /**
     * 获取哪些k线
     *
     * @param symbol       symbol
     * @param intervalList intervalList
     * @return KLineDataPublisher
     */
    @Override
    public MemoryKLineDataPublisher addListenKLine(
            String symbol,
            List<KLineInterval> intervalList
    ) {
        streamCommandBuilder.symbol(symbol);

        intervalList.forEach(kLineInterval -> {
            String key = getKLineMapKey(symbol, kLineInterval);

            final CustomBlockingQueue<KLine> buffer = new CustomBlockingQueue<>(bufferSize);
            realTimeKLineBufferMap.put(key, buffer);

            streamCommandBuilder.addSubscribeEntity(
                    StreamSubscribeEntity
                            .builder()
                            .symbol(symbol)
                            .subscribeType(WebSocketStreamType.KLINE)
                            .invocationHandler((streamName, result) -> {
                                //分发订阅的k线
                                KLine kLine = KLineMapper.mapJsonToKLine(result);
                                kLine.setKLineInterval(kLineInterval);
                                dispatchKLineData(key, kLine);
                            })
                            .callbackExecutor(publishExecutor)
                            .build()
                            .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, kLineInterval.getDescribe())
            );
        });
        streamCommandBuilder.subscribe();
        return this;
    }

    /**
     * 对订阅者分发k线数据
     *
     * @param key   key
     * @param kLine kLine
     */
    private void dispatchKLineData(String key, KLine kLine) {
        CustomBlockingQueue<KLine> buffer = realTimeKLineBufferMap.get(key);
        if (buffer == null) {
            log.warn("no kline data buffer [{}}", key);
        } else {
            buffer.offer(kLine);
        }
    }


    /**
     * 注册监听k线， 不及时消费可能会阻塞线程池！
     *
     * @param symbol   symbol
     * @param interval interval
     * @return SubscribeData
     */
    public KLineBuffer registry(String symbol, KLineInterval interval, LocalDateTime startTime) {

        String bufferKey = getKLineMapKey(symbol, interval);

        CustomBlockingQueue<KLine> buffer = realTimeKLineBufferMap.get(bufferKey);

        if (buffer == null) {
            throw new IllegalArgumentException("this publisher didn't listen kline: " + bufferKey);
        }

        KLineBuffer kb = new KLineBuffer(bufferSize);
        historyKLineLoader.startLoad(
                symbol,
                interval,
                startTime,
                kLineList -> {
                    try {
                        for (KLine kLine : kLineList) {
                            kLine.setKLineInterval(interval);
                            kb.put(kLine);
                        }
                    } catch (InterruptedException e) {
                        log.error("put kline data into kline buffer [{}] error", kb, e);
                        throw new RuntimeException(e);
                    }
                }
        ).thenAcceptAsync((endTime) -> {
            try {
                KLine kLine = null;
                while ((kLine = buffer.take()) != null && !KLine.STREAM_END_KLINE.equals(kLine)){
                    kb.put(kLine);
                    log.debug("put real time kline, current kline buffer size[{}]", kb.size());
                }
            } catch (InterruptedException e) {
                log.error("put kline data into kline buffer [{}] error", kb, e);
                throw new RuntimeException(e);
            }
        }, publishExecutor);
        return kb;
    }


    /**
     * 计算key
     *
     * @param symbol        symbol
     * @param kLineInterval kLineInterval
     * @return key
     */
    private static String getKLineMapKey(String symbol, KLineInterval kLineInterval) {
        return symbol + "-" + kLineInterval.getDescribe();
    }

}
