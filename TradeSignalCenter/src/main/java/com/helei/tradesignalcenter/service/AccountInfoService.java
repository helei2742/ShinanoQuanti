
package com.helei.tradesignalcenter.service;


import com.alibaba.fastjson.JSONObject;
import com.helei.dto.ASKey;
import com.helei.dto.account.AccountLocationConfig;
import com.helei.dto.account.UserInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * 账户信息服务
 */
@Getter
public class AccountInfoService {

    /**
     * uid  map  userInfo
     */
    private final Map<String, UserInfo> uid2UserInfo = new HashMap<>();

    /**
     * symbol map 订阅的uid
     */
    private final Map<String, List<String>> symbol2UIdsMap = new HashMap<>();


    /**
     * 获取订阅 symbol 交易对的账户的最新信息
     * @param symbol symbol
     * @return List<CompletableFuture<JSONObject>>
     */
    public List<CompletableFuture<JSONObject>> getNewestSubscribedAccount(String symbol) {

        List<CompletableFuture<JSONObject>> list = new ArrayList<>();

        return list;
    }

    /**
     * 根据uId获取账户的仓位设置
     * @param uid uid
     * @return AccountLocationConfig
     */
    public AccountLocationConfig getAccountLocationConfig(String uid) {
        return uid2UserInfo.get(uid).getAccountLocationConfig();
    }

    /**
     * 根据uid获取账户的 asKey
     * @param uid uid
     * @return ASKey
     */
    public ASKey getASKey(String uid) {
        return uid2UserInfo.get(uid).getAsKey();
    }

    /**
     * 初始化 ws
     */
    public void init() {
    }
}
