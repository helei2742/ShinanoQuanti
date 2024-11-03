package com.helei.binanceapi.api.rest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.BinanceRestApiClient;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.binanceapi.supporter.KLineMapper;
import com.helei.constants.KLineInterval;
import com.helei.dto.trade.KLine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BinanceUContractMarketRestApi extends BinanceRestApiClient {

    public BinanceUContractMarketRestApi(
            ExecutorService executor,
            String baseUrl,
            IpWeightSupporter ipWeightSupporter
    ) {
        super(executor, baseUrl, ipWeightSupporter);
    }


    public CompletableFuture<List<KLine>> queryKLines(
        String symbol,
        KLineInterval interval,
        Long startTime,
        Long endTime,
        Integer limit
    ) {

        JSONObject query = new JSONObject();
        query.put("symbol", symbol);
        query.put("interval", interval.getDescribe());
        if (startTime != null) {
            query.put("startTime", startTime);
        }
        if (endTime != null) {
            query.put("endTime", endTime);
        }
        if (limit != null) {
            query.put("limit", limit);
        } else {
            limit = 500;
        }

        int ipWeight;
        if (limit >= 1 && limit < 100) {
            ipWeight = 1;
        } else if(limit >= 100 && limit < 500) {
            ipWeight = 2;
        } else if(limit >= 500 && limit < 1000) {
            ipWeight = 5;
        } else {
            ipWeight = 10;
        }

        return request(
            "get",
                "/fapi/v1/klines",
                query,
                null,
                ipWeight
        ).thenApplyAsync(resp->{
            JSONArray data = JSONArray.parseArray(resp);
            List<KLine> kLines = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONArray jsonArray = data.getJSONArray(i);
                KLine kLine = KLineMapper.mapJsonArrToKLine(jsonArray);
                kLines.add(kLine);
            }
            return kLines;
        }, executor);
    }
}
