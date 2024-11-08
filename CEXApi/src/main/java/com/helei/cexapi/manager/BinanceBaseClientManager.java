

package com.helei.cexapi.manager;


import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.BinanceWSClientType;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.cexapi.client.BinanceWSEnvClient;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.KeyValue;
import com.helei.dto.config.RunTypeConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;


@Slf4j
public class BinanceBaseClientManager {

    /**
     * 运行设置a
     */
    private final RunTypeConfig runTypeConfig;

    /**
     * 不同环境的客户端
     */
    private final ConcurrentMap<String, BinanceWSEnvClient> envClientMap;

    @Getter
    private final IpWeightSupporter ipWeightSupporter = new IpWeightSupporter("localIp");

    /**
     * 执行的线程池
     */
    private final ExecutorService executor;

    public BinanceBaseClientManager(
            RunTypeConfig runTypeConfig,
            ExecutorService executor
    ) {

        this.runTypeConfig = runTypeConfig;
        this.executor = executor;
        this.envClientMap = new ConcurrentHashMap<>();

        for (KeyValue<RunEnv, TradeType> keyValue : runTypeConfig.getRunTypeList()) {
            RunEnv env = keyValue.getKey();
            TradeType tradeType = keyValue.getValue();
            envClientMap.put(buildKey(env, tradeType), new BinanceWSEnvClient(env, tradeType, this));
        }
    }


    /**
     * 获取指定环境、指定类型的币安连接客户端
     *
     * @param runEnv     运行环境
     * @param tradeType  交易类型
     * @param clientType 币安WS客户端类型
     * @return CompletableFuture<AbstractBinanceWSApiClient>
     */
    public CompletableFuture<AbstractBinanceWSApiClient> getEnvTypedApiClient(
            RunEnv runEnv,
            TradeType tradeType,
            BinanceWSClientType clientType
    ) {
        return getEnvTypedApiClient(runEnv, tradeType, clientType, null);
    }


    /**
     * 获取指定环境、指定类型的币安连接客户端
     *
     * @param runEnv     运行环境
     * @param tradeType  交易类型
     * @param clientType 币安WS客户端类型
     * @param mark       标记字段，用于标记不同的WS客户端
     * @return CompletableFuture<AbstractBinanceWSApiClient>
     */
    public CompletableFuture<AbstractBinanceWSApiClient> getEnvTypedApiClient(
            RunEnv runEnv,
            TradeType tradeType,
            BinanceWSClientType clientType,
            String mark
    ) {

        return CompletableFuture.supplyAsync(() -> {
            // 1 参数校验
            String key = buildKey(runEnv, tradeType);

            BinanceWSEnvClient binanceWSEnvClient = envClientMap.get(key);

            if (binanceWSEnvClient == null) {
                log.error("runEnv[{}]-tradeType[{}] 并未在环境注册, 不能创建此类api客户端, 请检查设置", runEnv, tradeType);
                return null;
            }

            // 2 获取或创建客户端
            AbstractBinanceWSApiClient client = null;
            try {
                client = binanceWSEnvClient.getOrCreateTypedClient(clientType, mark);

                client.setRunEnv(runEnv);
                client.setTradeType(tradeType);
            } catch (Exception e) {
                throw new RuntimeException(String.format("创建runEnv[%s]-tradeType[%s]-type[%s]客户端发生错误", runEnv, tradeType, clientType.name()), e);
            }

            try {
                // 3 启动客户端
                client.connect().get();
            } catch (Exception e) {
                throw new RuntimeException(String.format("启动runEnv[%s]-tradeType[%s]-type[%s]客户端发生错误", runEnv, tradeType, clientType.name()), e);
            }

            return client;
        }, executor);
    }


    /**
     * 构建key
     *
     * @param runEnv    运行环境
     * @param tradeType 交易类型
     * @return key
     */
    public static String buildKey(RunEnv runEnv, TradeType tradeType) {
        return runEnv.name() + "-" + tradeType.name();
    }
}

