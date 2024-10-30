package com.helei.dto.account;

import com.helei.dto.AssetInfo;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AccountBalanceInfo {

    private String userId;

    private String accountType;

    private Long updateTime;

    private List<AssetInfo> balances;

    /**
     * 获取账户可用的usdt
     * @return 可用的usdt
     */
    public Double getFreeUsdt() {
        if (balances == null || balances.isEmpty()) return 0.0;

        for (AssetInfo balance : balances) {
            if (balance.getAsset().equalsIgnoreCase("usdt")) {
                return balance.getFree();
            }
        }
        return 0.0;
    }
}
