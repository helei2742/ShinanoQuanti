package com.helei.tradeapplication.supporter;

import com.helei.constants.order.OrderType;
import com.helei.constants.order.TimeInForce;
import com.helei.dto.account.AccountPositionConfig;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.PositionInfo;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrder;
import com.helei.dto.order.type.*;
import com.helei.dto.account.BalanceInfo;
import com.helei.dto.trade.TradeSignal;
import com.helei.snowflack.BRStyle;
import com.helei.snowflack.SnowFlakeFactory;
import com.helei.util.OrderQuantityCalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


/**
 * 构建用于交易的订单，自带id
 */
@Component
public class TradeOrderBuildSupporter {

    @Autowired
    private SnowFlakeFactory snowFlakeFactory;

    /**
     * 构建限价单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param signal        信号
     * @return 限价单
     */
    public LimitOrder buildLimitOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, TradeSignal signal) {
        // Step 1 参数检验
        BigDecimal enterPrice = signal.getEnterPrice();
        if (enterPrice == null) return null;

        AccountPositionConfig positionConfig = accountInfo.getAccountPositionConfig();

        // Step 2 创建订单
        String symbol = signal.getSymbol();


        // Step 2.1 基础订单信息
        BaseOrder baseOrder = BaseOrder.builder()
                .orderId(nextId(OrderType.LIMIT))
                .runEnv(accountInfo.getRunEnv())
                .tradeType(accountInfo.getTradeType())
                .cexType(accountInfo.getCexType())
                .symbol(symbol)
                .side(signal.getTradeSide())
                .positionSide(positionConfig.getPositionSide())
                .userId(accountInfo.getUserId())
                .accountId(accountRTData.getAccountId())
                .build();
        LimitOrder limitOrder = new LimitOrder(baseOrder);

        // Step 2.2 订单价格、数量等 LimitOrder 信息
        BalanceInfo balanceInfo = accountRTData.getAccountBalanceInfo().getBalances().get(symbol);
        PositionInfo positionInfo = accountRTData.getAccountPositionInfo().getPositions().get(symbol);


        double quantity = OrderQuantityCalUtil.riskPercentBasedQuantityCalculate(
                balanceInfo.getFree(),
                positionConfig.getRiskPercent(),
                enterPrice.doubleValue(),
                positionInfo.getEnterPosition(),
                positionInfo.getPosition(),
                signal.getStopPrice().doubleValue()
        );

        limitOrder.setTimeInForce(TimeInForce.GTC);
        limitOrder.setPrice(enterPrice);
        limitOrder.setQuantity(BigDecimal.valueOf(quantity));

        return limitOrder;
    }


    /**
     * 构建市价单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param signal        信号
     * @return 限价单
     */
    public MarketOrder buildMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, TradeSignal signal) {
        return null;
    }

    /**
     * 构建市价止损单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    public StopLossMarketOrder buildStopMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        return null;
    }


    /**
     * 构建限价止损单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    public StopLossLimitOrder buildStopLimitOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        return null;
    }

    /**
     * 构建市价止盈单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    public TakeProfitMarketOrder buildTakeProfitMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        return null;
    }

    /**
     * 构建限价止盈单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    public TakeProfitLimitOrder buildTakeProfitLimitOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        return null;
    }

    /**
     * buildTrailingSTIDMarketOrder
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    public CEXTradeOrder buildTrailingSTIDMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        return null;
    }


    /**
     * 获取下一id
     * @param orderType 订单类型
     * @return 订单id
     */
    private String nextId(OrderType orderType) {
        return snowFlakeFactory.nextId(BRStyle.TRADE_SIGNAL, orderType.name());
    }
}
