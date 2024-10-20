package com.helei.binanceapi.api.rest;

import cn.hutool.core.util.StrUtil;
import com.helei.binanceapi.base.BinanceRestApiClient;
import com.helei.binanceapi.dto.ASKey;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
@Slf4j
public class BinanceListenKeyApi extends BinanceRestApiClient {

    public BinanceListenKeyApi(
            VirtualThreadTaskExecutor executor,
            String baseUrl,
            IpWeightSupporter ipWeightSupporter
    ) {
        super(executor, baseUrl, ipWeightSupporter);
    }


    /**
     * 生产listenKye创建一个新的user data stream，返回值为一个listenKey，
     * 即websocket订阅的stream名称。
     * 如果该帐户具有有效的listenKey，则将返回该listenKey并将其有效期延长60分钟。
     *
     * @param asKey asKey
     * @return CompletableFuture<String> listenKey
     */
    public CompletableFuture<String> requestListenKey(ASKey asKey) {
        if (asKey == null || StrUtil.isBlank(asKey.getApiKey()) || StrUtil.isBlank(asKey.getSecretKey())) {
            log.error("获取listenKey缺少需要签名参数， [{}]", asKey);
            return CompletableFuture.supplyAsync(()->null);
        }
        return request(
                "post",
                "/fapi/v1/listenKey",
                null,
                null,
                1,
                asKey
        ).thenApplyAsync(resp -> {
            log.info("请求获取listenKey成功，响应结果:[{}]", resp);
            if (resp != null) {
                return resp.getString("listenKey");
            }
            return null;
        }, executor);
    }

    /**
     * 延长listenKey有效期，有效期延长至本次调用后60分钟
     * @param asKey asKey
     * @return CompletableFuture<String> listenKey
     */
    public CompletableFuture<String> lengthenListenKey(ASKey asKey) {
        if (asKey == null || StrUtil.isBlank(asKey.getApiKey()) || StrUtil.isBlank(asKey.getSecretKey())) {
            log.error("延长listenKey缺少需要签名参数， [{}]", asKey);
            return CompletableFuture.supplyAsync(()->null);
        }
        return request(
                "put",
                "/fapi/v1/listenKey",
                null,
                null,
                1,
                asKey
        ).thenApplyAsync(resp -> {
            log.info("延长listenKey成功，响应结果:[{}]", resp);
            if (resp != null) {
                return resp.getString("listenKey");
            }
            return null;
        }, executor);
    }

    /**
     * 关闭listenKey， 关闭某账户数据流
     * @param asKey asKey
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> closeListenKey(ASKey asKey) {
        if (asKey == null || StrUtil.isBlank(asKey.getApiKey()) || StrUtil.isBlank(asKey.getSecretKey())) {
            log.error("关闭listenKey缺少需要签名参数， [{}]", asKey);
            return CompletableFuture.supplyAsync(()->null);
        }
        return request(
                "delete",
                "/fapi/v1/listenKey",
                null,
                null,
                1,
                asKey
        ).thenAcceptAsync(resp -> {
            log.info("关闭listenKey成功，响应结果:[{}]", resp);
        }, executor);
    }

}
