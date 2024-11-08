package com.helei.cexapi.client;

import cn.hutool.core.util.StrUtil;
import com.helei.binanceapi.BinanceWSAccountEventStreamClient;
import com.helei.binanceapi.BinanceWSMarketStreamClient;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.cexapi.manager.BinanceBaseClientManager;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class BinanceWSEnvClient {

    private final static String DEFAULT_MARK = "default";

    private final RunEnv runEnv;

    private final TradeType tradeType;

    private final BinanceApiConfig.BinanceTypedUrl urlSet;

    private final ConcurrentMap<BinanceWSClientType, ConcurrentHashMap<String, AbstractBinanceWSApiClient>> typedApiClient;

    private final BinanceBaseClientManager clientManager;

    private final InetSocketAddress proxy;

    public BinanceWSEnvClient(RunEnv runEnv, TradeType tradeType, BinanceBaseClientManager clientManager) {
        this.runEnv = runEnv;
        this.tradeType = tradeType;
        this.clientManager = clientManager;

        BinanceApiConfig apiConfig = BinanceApiConfig.INSTANCE;
        this.urlSet = apiConfig.getEnvUrlSet(runEnv, tradeType);
        this.proxy = apiConfig.getProxy().getProxyAddress();

        this.typedApiClient = new ConcurrentHashMap<>();
    }

    /**
     * 获取或创建clientType类型的客户端
     *
     * @param clientType 客户端类型
     * @param mark       客户端标志，根据这个标志可以创建出多个同类型的客户端
     * @return 客户端
     */
    public AbstractBinanceWSApiClient getOrCreateTypedClient(BinanceWSClientType clientType, String mark) {
        if (StrUtil.isBlank(mark)) mark = DEFAULT_MARK;

        ConcurrentHashMap<String, AbstractBinanceWSApiClient> markMap = typedApiClient.compute(clientType, (k, v) -> {
            if (v == null) {
                v = new ConcurrentHashMap<>();
            }
            return v;
        });

        String finalMark = mark;

        AbstractBinanceWSApiClient apiClient = markMap.compute(mark, (k, v) -> {
            if (v == null) {
                try {
                    v = createClientByType(clientType, finalMark);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return v;
        });

        log.info("runEnv[{}]-tradeType[{}]获取到客户端[{}]-mark[{}]", runEnv, tradeType, clientType, clientType.name() + "[" + mark + "]");

        return apiClient;
    }

    /**
     * 根据类型创建客户端
     *
     * @param clientType 客户端类型
     * @param mark       标记
     * @return 客户端
     * @throws URISyntaxException 异常
     */
    private AbstractBinanceWSApiClient createClientByType(BinanceWSClientType clientType, String mark) throws URISyntaxException {
        AbstractBinanceWSApiClient client = switch (clientType) {
            case REQUEST_RESPONSE -> createReqRespApiClient();
            case MARKET_STREAM -> createMarketStreamApiClient();
            case ACCOUNT_STREAM -> createAccountStreamClient();
        };


        client.setName(clientType + "[" + mark + "]");
        client.setProxy(proxy);

        return client;
    }

    /**
     * 创建账户事件流客户端
     *
     * @return BinanceWSAccountEventStreamClient
     */
    private BinanceWSAccountEventStreamClient createAccountStreamClient() throws URISyntaxException {
        String url = urlSet.getWs_account_info_stream_url();

        return new BinanceWSAccountEventStreamClient(url, clientManager.getIpWeightSupporter());
    }

    /**
     * 创建市场流数据客户端
     *
     * @return BinanceWSMarketStreamClient
     * @throws URISyntaxException URISyntaxException
     */
    private BinanceWSMarketStreamClient createMarketStreamApiClient() throws URISyntaxException {
        String url = urlSet.getWs_market_stream_url();
        return new BinanceWSMarketStreamClient(url, clientManager.getIpWeightSupporter());
    }

    /**
     * 创建请求、响应客户端
     *
     * @return BinanceWSReqRespApiClient
     * @throws URISyntaxException URISyntaxException
     */
    private BinanceWSReqRespApiClient createReqRespApiClient() throws URISyntaxException {
        String url = urlSet.getWs_market_url();
        return new BinanceWSReqRespApiClient(url, clientManager.getIpWeightSupporter());
    }
}
