package com.helei.binanceapi.base;

import com.alibaba.fastjson.JSONObject;
import com.helei.dto.ASKey;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.binanceapi.util.SignatureUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class BinanceRestApiClient {
    private static final int RETRY_TIMES = 3;

    protected final ExecutorService executor;

    private final String baseUrl;

    private final IpWeightSupporter ipWeightSupporter;

    private OkHttpClient okHttpClient;


    public BinanceRestApiClient(
            ExecutorService executor,
            String baseUrl,
            IpWeightSupporter ipWeightSupporter
    ) {
        this(executor, baseUrl, ipWeightSupporter, null);
    }

    public BinanceRestApiClient(
            ExecutorService executor,
            String baseUrl,
            IpWeightSupporter ipWeightSupporter,
            InetSocketAddress proxy
    ) {
        this.executor = executor;
        this.baseUrl = baseUrl;
        this.ipWeightSupporter = ipWeightSupporter;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (proxy != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, proxy));
        }
        this.okHttpClient = builder.build();
    }

    public CompletableFuture<String> request(
            String method,
            String path,
            JSONObject params,
            JSONObject body,
            int weight
    ) {
        return request(method, path, params, body, weight, null);
    }


    /**
     * 发送请求，如果有asKey参数不为null，则会鉴权
     *
     * @param method method
     * @param path   path
     * @param params params
     * @param body   body
     * @param weight weight
     * @param asKey  asKey
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<String> request(
            String method,
            String path,
            JSONObject params,
            JSONObject body,
            int weight,
            ASKey asKey
    ) {
        return CompletableFuture.supplyAsync(() -> {

            if (!ipWeightSupporter.submitIpWeight(weight)) {
                log.error("ip 请求受到限制，取消发送");
                return null;
            }

            // 创建表单数据
            StringBuilder queryString = new StringBuilder();

            JSONObject allKV = new JSONObject();

            if (params != null) {
                params.keySet().stream().sorted().forEach(key -> {
                    allKV.put(key, params.get(key));
                    queryString.append(key).append("=").append(params.get(key)).append("&");
                });

                if (!queryString.isEmpty()) {
                    queryString.deleteCharAt(queryString.length() - 1);
                }
            }

            String url = baseUrl + path + "?" + queryString;
            FormBody.Builder bodyBuilder = new FormBody.Builder();

            if (body != null) {
                body.forEach((k, v) -> {
                    bodyBuilder.add(k, String.valueOf(v));
                    allKV.put(k, v);
                });
            }

            Request.Builder builder = new Request.Builder();

            if (asKey != null) {
                try {
                    url += "&signature=" + SignatureUtil.signatureHMAC(asKey.getSecretKey(), allKV);
                    builder.addHeader("X-MBX-APIKEY", asKey.getApiKey());
                } catch (Exception e) {
                    throw new RuntimeException("计算签名失败", e);
                }
            }


            // 创建 POST 请求
            builder.url(url);
            String upperCase = method.toUpperCase();
            if (upperCase.equals("GET")) {
                builder.get();
            } else {
                builder.method(upperCase, bodyBuilder.build());
            }

            Request request = builder.build();

            log.info("创建请求 url[{}], method[{}], is signature[{}] 成功，开始请求服务器", url, method, asKey != null);

            for (int i = 0; i < RETRY_TIMES; i++) {
                // 发送请求并获取响应
                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        return response.body() == null ? "{}" : response.body().string();
                    } else {
                        log.error("请求url [{}] 失败， code [{}]， {}", url, response.code(), response.body());
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    log.warn("请求[{}]超时，尝试重新请求 [{}/{}]", url, i, RETRY_TIMES);
                } catch (IOException e) {
                    log.error("请求url [{}] 失败", url, e);
                    throw new RuntimeException(e);
                }
            }

            return null;
        }, executor);
    }

    public void setProxy(InetSocketAddress proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (proxy != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, proxy));
        }
        okHttpClient = builder.build();
    }
}
