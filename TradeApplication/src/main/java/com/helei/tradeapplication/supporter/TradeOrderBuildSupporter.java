package com.helei.tradeapplication.supporter;

import com.helei.constants.order.OrderType;
import com.helei.constants.order.TimeInForce;
import com.helei.dto.account.*;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrder;
import com.helei.dto.order.type.*;
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
     * @param accountInfo 账户信息
     * @param signal      信号
     * @return 限价单
     */
    public LimitOrder buildLimitOrder(UserAccountInfo accountInfo, TradeSignal signal) {
        // Step 1 参数检验
        UserAccountStaticInfo staticInfo = accountInfo.getUserAccountStaticInfo();
        UserAccountRealTimeInfo realTimeInfo = accountInfo.getUserAccountRealTimeInfo();

        String quote = staticInfo.getQuote();


        BigDecimal enterPrice = signal.getEnterPrice();
        if (enterPrice == null) return null;

        AccountPositionConfig positionConfig = staticInfo.getAccountPositionConfig();

        // Step 2 创建订单
        String symbol = signal.getSymbol();


        // Step 2.1 基础订单信息
        BaseOrder baseOrder = BaseOrder.builder()
                .clientOrderId(nextId(OrderType.LIMIT))
                .runEnv(staticInfo.getRunEnv())
                .tradeType(staticInfo.getTradeType())
                .cexType(staticInfo.getCexType())
                .symbol(symbol)
                .side(signal.getTradeSide())
                .positionSide(positionConfig.getPositionSide())
                .userId(accountInfo.getUserId())
                .accountId(staticInfo.getId())
                .build();
        LimitOrder limitOrder = new LimitOrder(baseOrder);

        // Step 2.2 订单价格、数量等 LimitOrder 信息
        BalanceInfo balanceInfo = realTimeInfo.getAccountBalanceInfo().getBalances().get(quote);
        PositionInfo positionInfo = realTimeInfo.getAccountPositionInfo().getPositions().get(symbol.toUpperCase());


        BigDecimal quantity = OrderQuantityCalUtil.riskPercentBasedQuantityCalculate(
                balanceInfo.getAvailableBalance(),
                BigDecimal.valueOf(positionConfig.getRiskPercent()),
                enterPrice,
                positionInfo.getEntryPrice(),
                positionInfo.getPositionAmt(),
                signal.getStopPrice()
        );

        limitOrder.setTimeInForce(TimeInForce.GTC);
        limitOrder.setPrice(enterPrice);
        limitOrder.setQuantity(quantity);

        return limitOrder;
    }


    /**
     * 构建市价单
     *
     * @param accountInfo 账户信息
     * @param signal      信号
     * @return 限价单
     */
    public MarketOrder buildMarketOrder(UserAccountInfo accountInfo, TradeSignal signal) {
        return null;
    }

    /**
     * 构建市价止损单
     *
     * @param accountInfo 账户信息
     * @param symbol      交易对
     * @return 限价单
     */
    public StopLossMarketOrder buildStopMarketOrder(UserAccountInfo accountInfo, String symbol) {
        return null;
    }


    /**
     * 构建限价止损单
     *
     * @param accountInfo 账户信息
     * @param symbol      交易对
     * @return 限价单
     */
    public StopLossLimitOrder buildStopLimitOrder(UserAccountRealTimeInfo accountInfo, String symbol) {
        return null;
    }

    /**
     * 构建市价止盈单
     *
     * @param accountInfo 账户信息
     * @param symbol      交易对
     * @return 限价单
     */
    public TakeProfitMarketOrder buildTakeProfitMarketOrder(UserAccountRealTimeInfo accountInfo, String symbol) {
        return null;
    }

    /**
     * 构建限价止盈单
     *
     * @param accountInfo 账户信息
     * @param symbol      交易对
     * @return 限价单
     */
    public TakeProfitLimitOrder buildTakeProfitLimitOrder(UserAccountRealTimeInfo accountInfo, String symbol) {
        return null;
    }

    /**
     * buildTrailingSTIDMarketOrder
     *
     * @param accountInfo 账户信息
     * @param symbol      交易对
     * @return 限价单
     */
    public CEXTradeOrder buildTrailingSTIDMarketOrder(UserAccountRealTimeInfo accountInfo, String symbol) {
        return null;
    }


    /**
     * 获取下一id
     *
     * @param orderType 订单类型
     * @return 订单id
     */
    private String nextId(OrderType orderType) {
        return snowFlakeFactory.nextId(BRStyle.TRADE_SIGNAL, orderType.name());
    }
}
