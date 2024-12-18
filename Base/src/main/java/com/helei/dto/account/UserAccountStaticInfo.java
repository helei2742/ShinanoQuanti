package com.helei.dto.account;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccountStaticInfo {

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
    private volatile boolean usable = false;

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
     * 交易币种
     */
    private final String quote = "USDT";

    /**
     * 订阅的交易对
     */
    private List<String> subscribeSymbol;


    /**
     * 账户仓位设置
     */
    private AccountPositionConfig accountPositionConfig;
}
