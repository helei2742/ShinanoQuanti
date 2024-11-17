package com.helei.binanceapi.api.rest;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.config.BinanceApiConfig;
import com.helei.binanceapi.constants.api.BinanceRestApiType;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.binanceapi.util.SignatureUtil;
import com.helei.constants.RunEnv;
import com.helei.constants.api.AbstractRestApiSchema;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class BinanceRestHttpApiClient {

    private static final int RETRY_TIMES = 1;

    private final OkHttpClient okHttpClient;

    private final ExecutorService executor;

    private final IpWeightSupporter ipWeightSupporter;

    private final BinanceApiConfig binanceApiConfig;


    public BinanceRestHttpApiClient(
            BinanceApiConfig binanceApiConfig,
            ExecutorService executor
    ) {
        this.executor = executor;
        this.binanceApiConfig = binanceApiConfig;
        this.ipWeightSupporter = new IpWeightSupporter("123");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (binanceApiConfig.getProxy() != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, binanceApiConfig.getProxy().getProxyAddress()));
        }
        this.okHttpClient = builder.build();
    }


    /**
     * 发送请求
     *
     * @param runEnv             runEnv
     * @param tradeType          交易类型
     * @param binanceRestApiType rest接口类型
     * @param allParam           所有的参数
     * @return result 的future
     */
    public <R> CompletableFuture<R> queryBinanceApi(
            RunEnv runEnv,
            TradeType tradeType,
            BinanceRestApiType binanceRestApiType,
            JSONObject allParam
    ) {
        return queryBinanceApi(runEnv, tradeType, binanceRestApiType, allParam, null);
    }


    /**
     * 发送请求
     *
     * @param runEnv             runEnv
     * @param tradeType          交易类型
     * @param binanceRestApiType rest接口类型
     * @param allParam           所有的参数
     * @param asKey              用于签名
     * @return result 的future
     */
    public <R> CompletableFuture<R> queryBinanceApi(
            RunEnv runEnv,
            TradeType tradeType,
            BinanceRestApiType binanceRestApiType,
            JSONObject allParam,
            ASKey asKey
    ) {
        AbstractRestApiSchema restApiSchema = binanceRestApiType.getRestApiSchema();
        String path = restApiSchema.getTradeTypePathMap().get(tradeType);
        int ipWeight = restApiSchema.calculateIpWeight(allParam);

        BinanceApiConfig.BinanceTypedUrl envUrlSet = binanceApiConfig.getEnvUrlSet(runEnv, tradeType);

        String baseUrl = envUrlSet.getRest_api_url();

        return request(
                baseUrl,
                restApiSchema.getMethod(),
                path,
                ipWeight,
                restApiSchema.getQueryKey() == null ? Collections.emptySet() : new HashSet<>(restApiSchema.getQueryKey()),
                restApiSchema.getBodyKey() == null ? Collections.emptySet() : new HashSet<>(restApiSchema.getBodyKey()),
                allParam,
                asKey,
                restApiSchema.isSignature()
        ).thenApplyAsync(restApiSchema::requestResultHandler, executor);
    }


    /**
     * 发送请求
     *
     * @param baseUrl     baseUrl
     * @param method      方法
     * @param path        path
     * @param ipWeight    ipWeight
     * @param paramsKey   query参数的Key
     * @param bodyKey     body参数的Key
     * @param allParam    所有参数
     * @param asKey       ASKey
     * @param isSignature 是否签名
     * @return future
     */
    private CompletableFuture<String> request(
            String baseUrl,
            String method,
            String path,
            int ipWeight,
            Set<String> paramsKey,
            Set<String> bodyKey,
            JSONObject allParam,
            ASKey asKey,
            boolean isSignature
    ) {
        return CompletableFuture.supplyAsync(() -> {

            if (!ipWeightSupporter.submitIpWeight(ipWeight)) {
                log.error("ip 请求受到限制，取消发送");
                return null;
            }

            // 创建表单数据
            StringBuilder queryString = new StringBuilder();
            StringBuilder payload = new StringBuilder();

            FormBody.Builder bodyBuilder = new FormBody.Builder();

            allParam.keySet().stream().sorted().forEach(key -> {
                Object value = allParam.get(key);
                if (paramsKey.contains(key)) {
                    queryString.append(key).append("=").append(value).append("&");
                } else if (bodyKey.contains(key)) {
                    bodyBuilder.add(key, String.valueOf(value));
                } else {
                    return;
                }

                payload.append(key).append("=").append(value).append("&");
            });

            if (!queryString.isEmpty()) {
                queryString.deleteCharAt(queryString.length() - 1);
            }
            if (!payload.isEmpty()) {
                payload.deleteCharAt(payload.length() - 1);
            }

            Request.Builder builder = new Request.Builder();

            if (asKey != null) {
                try {
                    if (isSignature) {
                        if (!queryString.isEmpty()) queryString.append("&");
                        queryString.append("signature=").append(SignatureUtil.hmac256(asKey.getSecretKey(), payload.toString()));
                    }

                    builder.addHeader("X-MBX-APIKEY", asKey.getApiKey());
                } catch (Exception e) {
                    throw new RuntimeException("计算签名失败", e);
                }
            }
            String url = baseUrl + path + "?" + queryString;
            builder.url(url);

            String upperCase = method.toUpperCase();
            switch (upperCase) {
                case "GET" -> builder.get();
                case "POST" -> builder.post(bodyBuilder.build());
                case "PUT" -> builder.put(bodyBuilder.build());
                case "DELETE" -> builder.delete();
                case "HEAD" -> builder.head();
            }

            Request request = builder.build();

            log.info("创建请求 url[{}], method[{}], is signature[{}] 成功，开始请求服务器", url, method, asKey != null);

            Exception lastException = null;

            for (int i = 0; i < RETRY_TIMES; i++) {
                // 发送请求并获取响应
                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        return response.body() == null ? "" : response.body().string();
                    } else {
                        log.error("请求url [{}] 失败， code [{}]， {}", url, response.code(), response.body() == null ? "" : response.body().string());
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    log.warn("请求[{}]超时，尝试重新请求 [{}/{}]", url, i, RETRY_TIMES);
                    lastException = e;
                } catch (Exception e) {
                    log.error("请求url [{}] 失败", url, e);
                    lastException = e;
                }
            }

            throw new RuntimeException(String.format("请求[%s]重试次数超过限制", url), lastException);
        }, executor);
    }
}

