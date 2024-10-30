package com.helei.tradesignalcenter.conventor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.dto.account.AccountBalanceInfo;
import com.helei.dto.AssetInfo;

import java.util.ArrayList;
import java.util.List;

public class AccountInfoMapper {

    public static AccountBalanceInfo mapJsonToAccountInfo(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        JSONObject infoJSON = jsonObject.getJSONObject("result");

        AccountBalanceInfo accountBalanceInfo = new AccountBalanceInfo();
        accountBalanceInfo.setUserId(infoJSON.getString("uid"));
        accountBalanceInfo.setAccountType(infoJSON.getString("accountType"));

        List<AssetInfo> balances = new ArrayList<>();

        JSONArray bArr = infoJSON.getJSONArray("balances");
        for (int i = 0; i < bArr.size(); i++) {
            JSONObject jb = bArr.getJSONObject(i);
            balances.add(new AssetInfo(jb.getString("asset"), jb.getDouble("free"), jb.getDouble("locked")));
        }

        accountBalanceInfo.setBalances(balances);
        return accountBalanceInfo;
    }

}
