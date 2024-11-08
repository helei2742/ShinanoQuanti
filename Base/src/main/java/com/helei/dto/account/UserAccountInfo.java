package com.helei.dto.account;


import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccountInfo implements Serializable {

    /**
     * 账户id
     */
    private long id;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 验证key
     */
    private ASKey asKey;

    /**
     * 是否可用
     */
    private final AtomicBoolean usable = new AtomicBoolean(false);

    /**
     * 运行环境，测试网还是主网
     */
    private RunEnv runEnv;

    /**
     * 交易类型
     */
    private TradeType tradeType;

    /**
     * 账户交易所类型
     */
    private CEXType cexType = CEXType.BINANCE;

    /**
     * 订阅的交易对
     */
    private List<String> subscribeSymbol;

    /**
     * 账户详情
     */
    private UserAccountTradeDetail detail;

    /**
     * 账户仓位设置
     */
    private AccountPositionConfig accountPositionConfig;

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
}
