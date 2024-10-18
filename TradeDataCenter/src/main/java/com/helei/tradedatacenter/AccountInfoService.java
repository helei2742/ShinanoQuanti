
package com.helei.tradedatacenter;


import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.api.BinanceWSAccountApi;
import com.helei.cexapi.binanceapi.dto.ASKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AccountInfoService {

    /**
     * uid  map  userInfo
     */
    private final Map<String, UserInfo> uid2SubSymbolMap = new HashMap<>();

    /**
     * symbol map 订阅的uid
     */
    private final Map<String, List<String>> symbol2UIdsMap = new HashMap<>();

    private final BinanceWSAccountApi accountApi;

    public AccountInfoService(BinanceWSApiClient binanceWSApiClient) {
        this.accountApi = binanceWSApiClient.getAccountApi();
    }


    public List<CompletableFuture<JSONObject>> getNewestSubscribedAccount(String symbol) {

        List<CompletableFuture<JSONObject>> list = new ArrayList<>();
        for (String accountId : symbol2UIdsMap.get(symbol)) {
            ASKey asKey = uid2SubSymbolMap.get(accountId).getAsKey();

            CompletableFuture<JSONObject> future = accountApi.accountStatus(false, asKey);
            list.add(future);
        }

        return list;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class UserInfo {
        private String id;

        private List<String> subscribeSymbol;

        private ASKey asKey;
    }


}
