package com.helei.dto.account;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.helei.dto.BalanceInfo;
import com.helei.dto.LockObject;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class AccountBalanceInfo extends LockObject {

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
    private ConcurrentHashSet<BalanceInfo> balances = new ConcurrentHashSet<>();

    public synchronized void updateBalanceInfos(List<BalanceInfo> balanceInfos) {
        balances.clear();
        balances.addAll(balanceInfos);
    }
}
