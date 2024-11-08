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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public enum BinanceRestApiType {

    /**
     * 现货k线的rest接口
     */
    KLINE(new AbstractRestApiSchema(RestApiType.KLINE) {

        @Override
        public void initSchema(Map<TradeType, String> tradeTypePathMap, AbstractRestApiSchema restApiSchema) {
            tradeTypePathMap.put(TradeType.SPOT, "/api/v3/klines");
            tradeTypePathMap.put(TradeType.CONTRACT, "/api/v3/klines");

            restApiSchema.setMethod("GET");
            restApiSchema.setQueryKey(List.of(RestApiParamKey.symbol, RestApiParamKey.interval, RestApiParamKey.startTime, RestApiParamKey.endTime, RestApiParamKey.timeZone, RestApiParamKey.limit));
        }

        @Override
        public int calculateIpWeight(JSONObject allParams) {
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

        @Override
        public <R> R requestResultHandler(String result) {
            JSONArray data = JSONArray.parseArray(result);
            List<KLine> kLines = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONArray jsonArray = data.getJSONArray(i);
                KLine kLine = KLineMapper.mapJsonArrToKLine(jsonArray);
                kLines.add(kLine);
            }
            return (R) kLines;
        }
    }),

    /**
     * 获取listenKey
     */
    QUERY_LISTEN_KEY(new AbstractRestApiSchema( RestApiType.QUERY_LISTEN_KEY) {

        @Override
        public void initSchema(Map<TradeType, String> tradeTypePathMap, AbstractRestApiSchema restApiSchema) {
            tradeTypePathMap.put(TradeType.SPOT, "/api/v3/userDataStream");
            tradeTypePathMap.put(TradeType.CONTRACT, "/api/v3/listenKey");

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
   LENGTH_LISTEN_KEY(new AbstractRestApiSchema(RestApiType.LENGTH_LISTEN_KEY) {
        @Override
        public void initSchema(Map<TradeType, String> tradeTypePathMap, AbstractRestApiSchema restApiSchema) {
            tradeTypePathMap.put(TradeType.SPOT, "/api/v3/userDataStream");
            tradeTypePathMap.put(TradeType.CONTRACT, "/api/v3/listenKey");

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
    REMOVE_LISTEN_KEY(new AbstractRestApiSchema(RestApiType.REMOVE_LISTEN_KEY) {
        @Override
        public void initSchema(Map<TradeType, String> tradeTypePathMap, AbstractRestApiSchema restApiSchema) {
            tradeTypePathMap.put(TradeType.SPOT, "/api/v3/userDataStream");
            tradeTypePathMap.put(TradeType.CONTRACT, "/api/v3/listenKey");

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



    private final AbstractRestApiSchema restApiSchema;

    BinanceRestApiType(AbstractRestApiSchema restApiSchema) {
        this.restApiSchema = restApiSchema;
    }
}
