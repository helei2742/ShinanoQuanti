package com.helei.dto.account;

import com.helei.dto.BalanceInfo;
import com.helei.dto.LockObject;
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
    private ConcurrentHashMap<String, BalanceInfo> balances = new ConcurrentHashMap<>();



    public void updateBalanceInfos(List<BalanceInfo> balanceInfos) {

        balanceInfos.forEach(balanceInfo -> {
            balances.put(balanceInfo.getAsset(), balanceInfo);
        });
    }
}
