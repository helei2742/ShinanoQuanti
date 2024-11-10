package com.helei.dto.account;

import com.alibaba.fastjson.annotation.JSONField;
import com.helei.dto.base.LockObject;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class AccountBalanceInfo extends LockObject implements Serializable {

    /**
     * 账户id
     */
    private long accountId;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 资金信息
     */
    @JSONField(name = "balances")
    private final ConcurrentHashMap<String, BalanceInfo> balances = new ConcurrentHashMap<>();



    public void updateBalanceInfos(List<BalanceInfo> balanceInfos) {
        long updateTime = System.currentTimeMillis();
        this.updateTime = updateTime;
        balanceInfos.forEach(balanceInfo -> {
            balanceInfo.setUpdateTime(updateTime);
            balances.put(balanceInfo.getAsset(), balanceInfo);
        });
    }
}
