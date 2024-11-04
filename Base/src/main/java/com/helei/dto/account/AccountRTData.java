package com.helei.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 账户实时数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRTData {

    private long userId;

    private long accountId;


    /**
     * 账户资金信息
     */
    private AccountBalanceInfo accountBalanceInfo;


    /**
     * 账户仓位信息
     */
    private AccountPositionInfo accountPositionInfo;
}
