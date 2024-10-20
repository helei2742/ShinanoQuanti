package com.helei.binanceapi.base;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.dto.ASKey;
import com.helei.binanceapi.dto.StreamSubscribeEntity;
import com.helei.dto.WebSocketCommandBuilder;
import com.helei.binanceapi.supporter.BinanceWSStreamSupporter;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.binanceapi.util.SignatureUtil;
import com.helei.binanceapi.constants.command.BaseCommandType;
import com.helei.netty.base.AbstractWebsocketClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 币按ws接口客户端抽象类
 */
@Slf4j
public class AbstractBinanceWSApiClient extends AbstractWebsocketClient<JSONObject, JSONObject> {

    /**
     * 处理ip限制相关
     */
    private final IpWeightSupporter ipWeightSupporter;


    /**
     * 处理stream流相关
     */
    private final BinanceWSStreamSupporter binanceWSStreamSupporter;

    public AbstractBinanceWSApiClient(
            String url,
            IpWeightSupporter ipWeightSupporter,
            BinanceWSStreamSupporter binanceWSStreamSupporter,
            AbstractBinanceWSApiClientHandler handler
    ) throws URISyntaxException {
        super(url, handler);

        this.ipWeightSupporter = ipWeightSupporter;

        this.binanceWSStreamSupporter = binanceWSStreamSupporter;
    }


    @Override
    public String getIdFromRequest(JSONObject request) {
        return request.getString("id");
    }

    @Override
    public void submitStreamResponse(String streamName, JSONObject message) {
        binanceWSStreamSupporter.publishStreamResponse(streamName, message, callbackInvoker);
    }


    /**
     * 发生请求
     *
     * @param ipWeight ip weight
     * @param request  请求体
     * @param callback 回调
     */
    public void sendRequest(
            int ipWeight,
            JSONObject request,
            Consumer<JSONObject> callback
    ) {
        sendRequest(ipWeight, request, null, callback);
    }

    /**
     * 发生请求
     *
     * @param ipWeight ip weight
     * @param request  请求体
     * @param asKey    签名参数
     * @param callback 回调
     */
    public void sendRequest(
            int ipWeight,
            JSONObject request,
            ASKey asKey,
            Consumer<JSONObject> callback
    ) {
        try {
            if (ipWeightSupporter.submitIpWeight(ipWeight)) {
                String id = getIdFromRequest(request);

                //需要签名
                if (asKey != null) {
                    JSONObject params = request.getJSONObject("params");
                    params.put("timestamp", System.currentTimeMillis());
                    try {
                        params.put("signature", SignatureUtil.signatureHMAC(asKey.getSecretKey(), params));
                    } catch (InvalidKeyException e) {
                        throw new IllegalArgumentException("signature params error");
                    }
                    params.put("apiKey", asKey.getApiKey());
                }

                super.sendRequest(request, response -> {
                    if (response != null) {

                        if (response.getInteger("status") != null && response.getInteger("status") != 200) {
                            log.error("receive error response [{}]", response);
                        }
                        log.debug("send request id[{}] success, response[{}]", id, response);
                        callback.accept(response);
                    } else {
                        callback.accept(null);
                        log.error("send request id[{}] fail", id);
                    }
                });
            } else {
                log.warn("current ipWeight[{}] not support send request", ipWeightSupporter.currentWeight());
            }
        } catch (Exception e) {
            log.error("send request error", e);
        }
    }

    /**
     * 发生请求
     *
     * @param ipWeight ip weight
     * @param request  请求体
     * @param asKey    签名
     */
    public CompletableFuture<JSONObject> sendRequest(
            int ipWeight,
            JSONObject request,
            ASKey asKey
    ) {
        if (!ipWeightSupporter.submitIpWeight(ipWeight)) {
            log.error("ipWeight[{}] not support send request", ipWeightSupporter.currentWeight());
            return null;
        }
        return super.sendRequest(trySignatureRequest(request, asKey))
                .thenApplyAsync(
                        jb -> {
                            if (jb == null || jb.getInteger("status") != 200) {
                                log.error("请求不成功，响应为 [{}]", jb);
                                throw new RuntimeException("请求不成功");
                            }
                            return jb;
                        }
                );
    }


    /**
     * 如果askey不为空，则对请求进行签名
     * @param request request
     * @param asKey asKey
     * @return JSONObject 签名后的请求
     */
    private JSONObject trySignatureRequest(JSONObject request, ASKey asKey) {
        //需要签名
        if (asKey != null && StrUtil.isNotBlank(asKey.getApiKey()) && StrUtil.isNotBlank(asKey.getSecretKey()) ) {
            JSONObject params = request.getJSONObject("params");
            params.put("timestamp", System.currentTimeMillis());
            params.put("apiKey", asKey.getApiKey());
            try {
                params.put("signature", SignatureUtil.signatureHMAC(asKey.getSecretKey(), params));
            } catch (Exception e) {
                log.error("signature params error", e);
                return null;
            }
        }
        return request;
    }


    /**
     * 订阅stream
     *
     * @param symbol  需订阅的币种symbol
     * @param subList 需订阅的类型
     */
    public void subscribeStream(String symbol, List<StreamSubscribeEntity> subList) {

        WebSocketCommandBuilder builder = WebSocketCommandBuilder.builder().setCommandType(BaseCommandType.SUBSCRIBE);

        /*
         * 由于StreamSubscribeEntity中可能存在鉴权的ASKey，需要每个单独发请求
         */
        for (StreamSubscribeEntity subscribeEntity : subList) {
            builder.clear();
            builder.addArrayParam(subscribeEntity.getStreamName());
            JSONObject command = builder.build();

            log.info("subscribe stream command: {}", command);

            String id = command.getString("id");

            sendRequest(1, command, subscribeEntity.getAsKey(), response -> {
                if (response != null) {
                    log.debug("get subscribe response: {}", response);
                    binanceWSStreamSupporter.addSubscribe(symbol, List.of(subscribeEntity));
                } else {
                    log.error("get subscribe response error, requestId[{}]", id);
                }
            });
        }
    }
}
