package com.helei.constants.api;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.trade.TradeType;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public abstract class AbstractRestApiSchema {

    private static final Map<RestApiType, AbstractRestApiSchema> TYPE_MAP = new HashMap<>();

    /**
     * 交易类型和path的map
     */
    private Map<TradeType, String> tradeTypePathMap = new HashMap<>();

    /**
     * api类型
     */
    private final RestApiType restApiType;


    /**
     * 请求方法
     */
    private String method;


    /**
     * 请求的query参数的Key
     */
    private List<String> queryKey;

    /**
     * 请求body参数的key
     */
    private List<String> bodyKey;

    /**
     * 是否签名，当需要签名，并且请求参数没带签名参数时，抛出异常
     */
    private boolean isSignature = false;

    protected AbstractRestApiSchema(RestApiType restApiType) {
        this.restApiType = restApiType;
        TYPE_MAP.put(restApiType, this);

        initSchema(tradeTypePathMap,this);
    }

    public abstract void initSchema(Map<TradeType, String> tradeTypePathMap, AbstractRestApiSchema restApiSchema);

    public abstract int calculateIpWeight(JSONObject allParams);

    public abstract <R> R requestResultHandler(String result);


    @Override
    public String toString() {
        return method + " " + tradeTypePathMap + " query [" + queryKey + "] body [" + bodyKey + "]";
    }
}
