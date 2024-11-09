package com.helei.dto.account;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccountRealTimeInfo implements Serializable {
    /**
     * 账户id
     */
    private long id;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 账户详情
     */
    private UserAccountTradeDetail detail;


    /**
     * 账户资金信息
     */
    private final AccountBalanceInfo accountBalanceInfo = new AccountBalanceInfo();

    /**
     * 账户仓位信息
     */
    private final AccountPositionInfo accountPositionInfo = new AccountPositionInfo();

    public void setId(long id) {
        this.id = id;
        this.accountBalanceInfo.setAccountId(id);
        this.accountPositionInfo.setAccountId(id);
    }


    public static UserAccountRealTimeInfo generateAccountStatusFromJson(JSONObject result) {
        UserAccountRealTimeInfo userAccountRealTimeInfo = new UserAccountRealTimeInfo();

        JSONObject content = result.getJSONObject("result");

        //Step 3.1 解析UserAccountTradeDetail
        userAccountRealTimeInfo.setDetail(UserAccountTradeDetail.generateFromJSON(content));

        //Step 3.2 解析资产信息
        List<BalanceInfo> balanceInfos = new ArrayList<>();
        JSONArray assets = content.getJSONArray("assets");
        for (int i = 0; i < assets.size(); i++) {
            balanceInfos.add(BalanceInfo.fromJson(assets.getJSONObject(i)));
        }
        userAccountRealTimeInfo.getAccountBalanceInfo().updateBalanceInfos(balanceInfos);

        //Step 3.3 解析仓位信息
        List<PositionInfo> positionInfos = new ArrayList<>();
        JSONArray positions = content.getJSONArray("positions");
        for (int i = 0; i < positions.size(); i++) {
            positionInfos.add(PositionInfo.fromJson(positions.getJSONObject(i)));
        }
        userAccountRealTimeInfo.getAccountPositionInfo().updatePositionInfos(positionInfos);

        return userAccountRealTimeInfo;
    }
}
