package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.api.ws.BinanceWSStreamApi;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.cexapi.CEXApiFactory;
import com.helei.constants.KLineInterval;
import com.helei.constants.WebSocketStreamParamKey;
import com.helei.dto.KLine;

import com.helei.tradesignalcenter.conventor.KLineMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * 内存的k线数据源
 */
@Slf4j
public class MemoryKLineSource extends BaseKLineSource {

    /**
     * 流api 获取流实时数据
     */
    private transient BinanceWSApiClient streamApiClient;

    /**
     * 普通api 用于获取历史数据
     */
    private transient BinanceWSApiClient normalApiClient;


    private transient VirtualThreadTaskExecutor publishExecutor;

    private final long startTime;

    private final String streamUrl;

    private final String requestUrl;

    private final int historyLoadBatch;


    @Override
    public void open(OpenContext openContext) throws Exception {
        publishExecutor = new VirtualThreadTaskExecutor("kline-load-executor");

        // Step 1: 初始化ApiClient
        normalApiClient = CEXApiFactory.binanceApiClient(requestUrl);
        normalApiClient.setName("历史k线获取客户端-" + UUID.randomUUID().toString().substring(0, 8));
        streamApiClient = CEXApiFactory.binanceApiClient(streamUrl);
        streamApiClient.setName("实时k线获取客户端-" + UUID.randomUUID().toString().substring(0, 8));
        CompletableFuture.allOf(normalApiClient.connect(), streamApiClient.connect()).get();


    }

    public MemoryKLineSource(
            String symbol,
            List<KLineInterval> intervals,
            long startTime,
            String streamUrl,
            String requestUrl,
            int historyLoadBatch
    ) {
        super(intervals, symbol);
        this.startTime = startTime;
        this.streamUrl = streamUrl;
        this.requestUrl = requestUrl;
        this.historyLoadBatch = historyLoadBatch;
    }

    @Override
    boolean loadData(SourceContext<KLine> sourceContext) throws ExecutionException, InterruptedException {

        HistoryKLineLoader historyKLineLoader = new HistoryKLineLoader(historyLoadBatch, normalApiClient, publishExecutor);

        for (KLineInterval interval : getIntervals()) {
            // Step 2: 开始历史k线获取
            log.info("symbol[{}] interval[{}] 开始获取历史k线， 开始时间[{}]", getSymbol(), interval, Instant.ofEpochMilli(startTime));

            historyKLineLoader.startLoad(getSymbol(), interval, startTime, kLines -> {
                for (KLine kLine : kLines) {
                    sourceContext.collect(kLine);
                }
            }).thenAcceptAsync((endTime) -> {
                // Step 3: 实时k获取
                log.info("symbol[{}] interval[{}] 历史k线获取完毕，开始获取实时数据", getSymbol(), interval);

                BinanceWSStreamApi.StreamCommandBuilder streamCommandBuilder = streamApiClient.getStreamApi().builder();
                streamCommandBuilder.symbol(getSymbol());

                streamCommandBuilder.addSubscribeEntity(
                        StreamSubscribeEntity
                                .builder()
                                .symbol(getSymbol())
                                .subscribeType(WebSocketStreamType.KLINE)
                                .invocationHandler((streamName, result) -> {

                                    KLine kLine = KLineMapper.mapJsonToKLine(result);
                                    kLine.setKLineInterval(interval);

                                    sourceContext.collect(kLine);
                                })
                                .callbackExecutor(publishExecutor)
                                .build()
                                .addParam(WebSocketStreamParamKey.KLINE_INTERVAL, interval.getDescribe())
                );
                streamCommandBuilder.subscribe();
                log.info("已发送获取kLine[symbol:{}, interval{}]请求", getSymbol(), interval);
            }, publishExecutor);
        }

        return true;
    }

    @Override
    void refreshState() {

    }
}
