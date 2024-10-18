package com.helei.tradedatacenter.conventor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.dto.AccountInfo;
import com.helei.tradedatacenter.dto.AssetInfo;

import java.util.ArrayList;
import java.util.List;

public class AccountInfoMapper {

    public static AccountInfo mapJsonToAccountInfo(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        JSONObject infoJSON = jsonObject.getJSONObject("result");

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setUId(infoJSON.getString("uid"));
        accountInfo.setAccountType(infoJSON.getString("accountType"));

        List<AssetInfo> balances = new ArrayList<>();

        JSONArray bArr = infoJSON.getJSONArray("balances");
        for (int i = 0; i < bArr.size(); i++) {
            JSONObject jb = bArr.getJSONObject(i);
            balances.add(new AssetInfo(jb.getString("asset"), jb.getDouble("free"), jb.getDouble("locked")));
        }

        accountInfo.setBalances(balances);
        return accountInfo;
    }

}
