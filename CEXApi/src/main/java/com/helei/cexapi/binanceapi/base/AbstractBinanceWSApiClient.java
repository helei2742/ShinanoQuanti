

package com.helei.cexapi.binanceapi.base;

        import com.alibaba.fastjson.JSONObject;
        import com.helei.cexapi.binanceapi.dto.ASKey;
        import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;
        import com.helei.cexapi.binanceapi.dto.WebSocketCommandBuilder;
        import com.helei.cexapi.binanceapi.supporter.BinanceWSStreamSupporter;
        import com.helei.cexapi.binanceapi.supporter.IpWeightSupporter;
        import com.helei.cexapi.binanceapi.util.SignatureUtil;
        import com.helei.cexapi.netty.base.AbstractWebsocketClient;
        import com.helei.cexapi.binanceapi.constants.command.BaseCommandType;
        import lombok.extern.slf4j.Slf4j;

        import javax.net.ssl.SSLException;
        import java.net.URISyntaxException;
        import java.security.InvalidKeyException;
        import java.security.NoSuchAlgorithmException;
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
            int threadPoolSize,
            String url,
            IpWeightSupporter ipWeightSupporter,
            BinanceWSStreamSupporter binanceWSStreamSupporter,
            AbstractBinanceWSApiClientHandler handler
    ) throws URISyntaxException, SSLException {
        super(threadPoolSize, url, handler);

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
     * @param ipWeight    ip weight
     * @param request     请求体
     * @param asKey         签名参数
     * @param callback    回调
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
     * @param ipWeight    ip weight
     * @param request     请求体
     * @param asKey 签名
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
        return super.sendRequest(trySignatureRequest(request, asKey));
    }

    private JSONObject trySignatureRequest(JSONObject request, ASKey asKey) {
        //需要签名
        if (asKey != null) {
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
