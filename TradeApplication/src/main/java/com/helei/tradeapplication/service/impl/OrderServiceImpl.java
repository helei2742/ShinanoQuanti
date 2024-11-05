package com.helei.tradeapplication.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.order.OrderEvent;
import com.helei.constants.order.OrderType;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.LimitOrder;
import com.helei.constants.CEXType;
import com.helei.constants.trade.TradeType;
import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.PositionInfo;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.trade.BalanceInfo;
import com.helei.dto.trade.TradeSignal;
import com.helei.interfaces.CompleteInvocation;
import com.helei.tradeapplication.manager.ExecutorServiceManager;
import com.helei.tradeapplication.service.IBinanceContractOrderService;
import com.helei.tradeapplication.service.OrderEventProcessService;
import com.helei.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class OrderServiceImpl extends OrderEventProcessService {


    @Autowired
    private KafkaProducerService kafkaProducerService;


    @Autowired
    private IBinanceContractOrderService binanceContractOrderService;


    @Autowired
    public OrderServiceImpl(ExecutorServiceManager executorServiceManager) {
        super(executorServiceManager.getOrderExecutor());
        super.startProcessEvents();
    }


    /**
     * 生成订单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param signal        信号
     */
    @Override
    public void makeOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, TradeSignal signal, CompleteInvocation<BaseOrder> invocation) {
        String symbol = signal.getSymbol().toLowerCase();

        OrderType orderType = accountInfo.getAccountPositionConfig().getOrderType();

        BaseOrder order = switch (orderType) {
            case LIMIT -> buildLimitOrder(accountInfo, accountRTData, symbol);
            case MARKET -> buildMarketOrder(accountInfo, accountRTData, symbol);
            case STOP_MARKET -> buildStopMarketOrder(accountInfo, accountRTData, symbol);
            case STOP_LIMIT -> buildStopLimitOrder(accountInfo, accountRTData, symbol);
            case TAKE_PROFIT_MARKET -> buildTakeProfitMarketOrder(accountInfo, accountRTData, symbol);
            case TAKE_PROFIT_LIMIT -> buildTakeProfitLimitOrder(accountInfo, accountRTData, symbol);
            case TRAILING_STIO_MARKET -> buildTrailingSTIDMarketOrder(accountInfo, accountRTData, symbol);
        };


        log.info("订单[{}]创建成功", order);
        // 提交订单创建事件
        super.submitOrderEvent(order, OrderEvent.CREATED_ORDER, invocation);
    }


    @Override
    public BaseOrder writeOrder2Kafka(BaseOrder order) throws ExecutionException, InterruptedException {
        String topic = KafkaUtil.getOrderSymbolTopic(order.getRunEnv(), order.getTradeType(), order.getSymbol());

        kafkaProducerService.sendMessage(topic, JSONObject.toJSONString(order)).get();

        log.debug("订单order[{}]写入kafka成功", order);

        return order;
    }


    @Override
    public BaseOrder writeOrder2DB(BaseOrder order) {
        CEXType cexType = order.getCexType();
        TradeType tradeType = order.getTradeType();

        if (CEXType.BINANCE.equals(cexType) && TradeType.CONTRACT.equals(tradeType)) {
            binanceContractOrderService.saveOrder(order);
        }

        return order;
    }


    /**
     * 构建限价单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    private static @NotNull LimitOrder buildLimitOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        BalanceInfo balanceInfo = accountRTData.getAccountBalanceInfo().getBalances().get(symbol);
        PositionInfo positionInfo = accountRTData.getAccountPositionInfo().getPositions().get(symbol);

        LimitOrder limitOrder = new LimitOrder();
        limitOrder.setRunEnv(accountInfo.getRunEnv());
        limitOrder.setTradeType(accountInfo.getTradeType());
        limitOrder.setCexType(accountInfo.getCexType());

        limitOrder.setSymbol(symbol);
        limitOrder.setUserId(accountInfo.getUserId());
        limitOrder.setAccountId(accountRTData.getAccountId());

        //TODO 完善逻辑

        return limitOrder;
    }


    /**
     * 构建市价单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param symbol        交易对
     * @return 限价单
     */
    private BaseOrder buildMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
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
    private BaseOrder buildStopMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
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
    private BaseOrder buildStopLimitOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
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
    private BaseOrder buildTakeProfitMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
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
    private BaseOrder buildTakeProfitLimitOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
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
    private BaseOrder buildTrailingSTIDMarketOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, String symbol) {
        return null;
    }

}


