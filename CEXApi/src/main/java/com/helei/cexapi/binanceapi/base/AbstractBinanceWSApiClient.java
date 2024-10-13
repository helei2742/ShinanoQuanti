package com.helei.cexapi.binanceapi.base;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;
import com.helei.cexapi.binanceapi.dto.WebSocketCommandBuilder;
import com.helei.cexapi.binanceapi.supporter.BinanceWSStreamSupporter;
import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;
import com.helei.cexapi.binanceapi.util.SignatureUtil;
import com.helei.cexapi.netty.base.AbstractWebsocketClient;
import com.helei.cexapi.binanceapi.constants.WebSocketCommandType;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    protected ASKey asKey;


    public AbstractBinanceWSApiClient(
            int threadPoolSize,
            String url,
            IpWeightSupporter ipWeightSupporter,
            BinanceWSStreamSupporter binanceWSStreamSupporter,
            AbstractBinanceWSApiClientHandler handler
    ) throws URISyntaxException {
        super(threadPoolSize, url, handler);

        this.ipWeightSupporter = ipWeightSupporter;

        this.binanceWSStreamSupporter = binanceWSStreamSupporter;
    }


    @Override
    public void submitStreamResponse(String streamName, JSONObject message) {
        binanceWSStreamSupporter.publishStreamResponse(streamName, message, callbackInvoker);
    }



    /**
     * 发生请求
     * @param ipWeight ip weight
     * @param id   请求的id
     * @param request   请求体
     * @param callback  回调
     */
    public void sendRequest(
            int ipWeight,
            String id,
            JSONObject request,
            Consumer<JSONObject> callback
    ) {
        sendRequest(ipWeight, id, request, false, callback);
    }
    /**
     * 发生请求
     * @param ipWeight ip weight
     * @param id   请求的id
     * @param request   请求体
     * @param isSignature   是否签名
     * @param callback  回调
     */
    public void sendRequest(
            int ipWeight,
            String id,
            JSONObject request,
            boolean isSignature,
            Consumer<JSONObject> callback
    ) {
        if (ipWeightSupporter.submitIpWeight(ipWeight)) {

            //需要签名
            if (isSignature) {
                JSONObject params = request.getJSONObject("params");
                params.put("timestamp", System.currentTimeMillis());
                try {
                    params.put("signature", SignatureUtil.signatureHMAC(asKey.getSecretKey(), params));
                } catch (InvalidKeyException e) {
                    throw new IllegalArgumentException("signature params error");
                }
                params.put("apiKey", asKey.getApiKey());
            }

            super.sendRequest(id, request, response -> {
                if (response != null) {
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
    }
    /**
     * 订阅stream
     * @param symbol 需订阅的币种symbol
     * @param subList 需订阅的类型
     */
    public void subscribeStream(String symbol, List<StreamSubscribeEntity> subList) {
        WebSocketCommandBuilder builder = WebSocketCommandBuilder.builder().setCommandType(WebSocketCommandType.SUBSCRIBE);

        AtomicBoolean isSignature = new AtomicBoolean(false);
        subList.forEach(e->{
            if (e.isSignature()) {
                isSignature.set(true);
            }
            builder.addArrayParam(e.getStreamName());
        });
        JSONObject command = builder.build();

        log.info("subscribe stream command: {}", command);

        String id = command.getString("id");

        sendRequest(1, id, command, isSignature.get(), response -> {
            if (response != null) {
                log.debug("get subscribe response: {}", response);
                binanceWSStreamSupporter.addSubscribe(symbol, subList);
            }else {
                log.error("get subscribe response error, requestId[{}]", id);
            }
        });
    }
}
