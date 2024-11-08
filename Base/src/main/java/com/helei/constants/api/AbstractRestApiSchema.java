package com.helei.constants.api;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.trade.TradeType;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public abstract class AbstractRestApiSchema {

    private static final Map<String, AbstractRestApiSchema> TYPE_MAP = new HashMap<>();


    /**
     * 交易类型
     */
    private final TradeType tradeType;

    /**
     * api类型
     */
    private final RestApiType restApiType;


    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求的路径
     */
    private String path;

    /**
     * 请求的query参数的Key
     */
    private List<String> queryKey;

    /**
     * 请求body参数的key
     */
    private List<String> bodyKey;


    protected AbstractRestApiSchema(TradeType tradeType, RestApiType restApiType) {
        this.tradeType = tradeType;
        this.restApiType = restApiType;
        TYPE_MAP.put(generateKey(tradeType, restApiType), this);

        initSchema(this);
    }

    public abstract void initSchema(AbstractRestApiSchema restApiSchema);


    public abstract int calculateIpWeight(JSONObject allParams);

    public abstract <R> R requestResultHandler(String result);

    /**
     * 构造Key
     *
     * @param tradeType   交易类型
     * @param restApiType restApi类型
     * @return key
     */
    private static String generateKey(TradeType tradeType, RestApiType restApiType) {
        return tradeType.name() + ":" + restApiType.name();
    }


    @Override
    public String toString() {
        return method + " " + path + " query [" + queryKey + "] body [" + bodyKey + "]";
    }
}
