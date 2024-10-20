package com.helei.binanceapi.base;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.dto.ASKey;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.binanceapi.util.SignatureUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class BinanceRestApiClient {

    protected final VirtualThreadTaskExecutor executor;

    private final String baseUrl;

    private final IpWeightSupporter ipWeightSupporter;

    private final OkHttpClient okHttpClient;


    public BinanceRestApiClient(
            VirtualThreadTaskExecutor executor,
            String baseUrl,
            IpWeightSupporter ipWeightSupporter
    ) {
        this.executor = executor;
        this.baseUrl = baseUrl;
        this.ipWeightSupporter = ipWeightSupporter;
        this.okHttpClient = new OkHttpClient();
    }


    /**
     * 发送请求，如果有asKey参数不为null，则会鉴权
     * @param method method
     * @param path path
     * @param params params
     * @param body body
     * @param weight weight
     * @param asKey asKey
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> request(
            String method,
            String path,
            JSONObject params,
            JSONObject body,
            int weight,
            ASKey asKey
    ) {
        return CompletableFuture.supplyAsync(()->{

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
                body.forEach((k,v)->{
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
            Request request = builder
                    .url(url)
                    .method(method, bodyBuilder.build())
                    .build();

            log.info("创建请求 url[{}], method[{}], is signature[{}] 成功，开始请求服务器", url, method, asKey != null);

            // 发送请求并获取响应
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String respStr = response.body() == null ? "{}" : response.body().string();
                    return JSONObject.parseObject(respStr);
                } else {
                    log.error("请求url [{}] 失败， code [{}]", url, response.code());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        }, executor);
    }
}
