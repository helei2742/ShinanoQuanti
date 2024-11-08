package com.helei.binanceapi.constants.api;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.supporter.KLineMapper;
import com.helei.constants.api.AbstractRestApiSchema;
import com.helei.constants.api.RestApiParamKey;
import com.helei.constants.api.RestApiType;
import com.helei.constants.trade.TradeType;
import com.helei.dto.trade.KLine;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum BinanceRestApiType {

    /**
     * 现货k线的rest接口
     */
    SPOT_KLINE(new AbstractRestApiSchema(TradeType.SPOT, RestApiType.KLINE) {

        @Override
        public void initSchema(AbstractRestApiSchema restApiSchema) {
            restApiSchema.setPath("/api/v3/klines");
            restApiSchema.setMethod("GET");
            restApiSchema.setQueryKey(List.of(RestApiParamKey.symbol, RestApiParamKey.interval, RestApiParamKey.startTime, RestApiParamKey.endTime, RestApiParamKey.timeZone, RestApiParamKey.limit));
        }

        @Override
        public int calculateIpWeight(JSONObject allParams) {
            return calculateKLineWeight(allParams);
        }

        @Override
        public <R> R requestResultHandler(String result) {
            return resolveKLineResult(result);
        }
    }),


    /**
     * 合约k线的rest接口
     */
    U_CONTRACT_KLINE(new AbstractRestApiSchema(TradeType.CONTRACT, RestApiType.KLINE) {

        @Override
        public void initSchema(AbstractRestApiSchema restApiSchema) {
            restApiSchema.setPath("/fapi/v1/klines");
            restApiSchema.setMethod("GET");
            restApiSchema.setQueryKey(List.of(RestApiParamKey.symbol, RestApiParamKey.interval, RestApiParamKey.startTime, RestApiParamKey.endTime, RestApiParamKey.timeZone, RestApiParamKey.limit));
        }

        @Override
        public int calculateIpWeight(JSONObject allParams) {
            return calculateKLineWeight(allParams);
        }

        @Override
        public <R> R requestResultHandler(String result) {
            return resolveKLineResult(result);
        }
    }),

    /**
     * 获取listenKey
     */
    QUERY_LISTEN_KEY(new AbstractRestApiSchema(TradeType.SPOT, RestApiType.QUERY_LISTEN_KEY) {
        @Override
        public void initSchema(AbstractRestApiSchema restApiSchema) {
            restApiSchema.setPath("/fapi/v1/listenKey");
            restApiSchema.setMethod("POST");
        }

        @Override
        public int calculateIpWeight(JSONObject allParams) {
            return 1;
        }

        @Override
        public <R> R requestResultHandler(String result) {
            JSONObject jb = JSONObject.parseObject(result);
            if (result != null) {
                return (R) jb.getString("listenKey");
            }
            return null;
        }
    }),
    /**
     * 延长listenKey
     */
    LENGTH_LISTEN_KEY(new AbstractRestApiSchema(TradeType.SPOT, RestApiType.LENGTH_LISTEN_KEY) {
        @Override
        public void initSchema(AbstractRestApiSchema restApiSchema) {
            restApiSchema.setPath("/fapi/v1/listenKey");
            restApiSchema.setMethod("PUT");
        }

        @Override
        public int calculateIpWeight(JSONObject allParams) {
            return 1;
        }

        @Override
        public <R> R requestResultHandler(String result) {
            JSONObject jb = JSONObject.parseObject(result);
            if (result != null) {
                return (R) jb.getString("listenKey");
            }
            return null;
        }
    }),
    /**
     * 注销listenKey
     */
    REMOVE_LISTEN_KEY(new AbstractRestApiSchema(TradeType.SPOT, RestApiType.REMOVE_LISTEN_KEY) {
        @Override
        public void initSchema(AbstractRestApiSchema restApiSchema) {
            restApiSchema.setPath("/fapi/v1/listenKey");
            restApiSchema.setMethod("DELETE+");
        }

        @Override
        public int calculateIpWeight(JSONObject allParams) {
            return 1;
        }

        @Override
        public <R> R requestResultHandler(String result) {
            JSONObject jb = JSONObject.parseObject(result);
            if (result != null) {
                return (R) jb.getString("listenKey");
            }
            return null;
        }
    }),

    ;


    /**
     * 解析k线结果
     *
     * @param result http 请求返回的内容
     * @param <R>    返回值类型
     * @return R
     */
    private static <R> @NotNull R resolveKLineResult(@NotNull String result) {
        JSONArray data = JSONArray.parseArray(result);
        List<KLine> kLines = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONArray jsonArray = data.getJSONArray(i);
            KLine kLine = KLineMapper.mapJsonArrToKLine(jsonArray);
            kLines.add(kLine);
        }
        return (R) kLines;
    }


    /**
     * 计算k获取k线请求的ip weight
     *
     * @param allParams 请求的所有参数
     * @return ip weight
     */
    private static int calculateKLineWeight(JSONObject allParams) {
        Integer limit = allParams.getInteger(RestApiParamKey.limit);

        if (limit == null) {
            limit = 500;
        }

        int ipWeight;
        if (limit >= 1 && limit < 100) {
            ipWeight = 1;
        } else if (limit >= 100 && limit < 500) {
            ipWeight = 2;
        } else if (limit >= 500 && limit < 1000) {
            ipWeight = 5;
        } else {
            ipWeight = 10;
        }

        return ipWeight;
    }


    private final AbstractRestApiSchema restApiSchema;

    BinanceRestApiType(AbstractRestApiSchema restApiSchema) {
        this.restApiSchema = restApiSchema;
    }
}
