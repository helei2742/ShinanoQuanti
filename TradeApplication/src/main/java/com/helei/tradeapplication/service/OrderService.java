package com.helei.tradeapplication.service;

import com.helei.dto.account.AccountRTData;
import com.helei.dto.account.UserAccountInfo;
import com.helei.tradeapplication.dto.GroupOrder;
import com.helei.dto.trade.TradeSignal;
import com.helei.interfaces.CompleteInvocation;

import java.util.concurrent.ExecutionException;

public interface OrderService {


    /**
     * 生成订单
     *
     * @param accountInfo   账户信息
     * @param accountRTData 账户实时数据
     * @param signal        信号
     * @param invocation    生成订单结果的回调
     */
    void makeOrder(UserAccountInfo accountInfo, AccountRTData accountRTData, TradeSignal signal, CompleteInvocation<GroupOrder> invocation);


    /**
     * 将订单写到kafka
     *
     * @param order 订单数据
     * @return 订单数据
     */
    GroupOrder writeOrder2Kafka(GroupOrder order) throws ExecutionException, InterruptedException;


    /**
     * 将订单写入数据库
     *
     * @param order order
     * @return 订单数据
     */
    GroupOrder writeOrder2DB(GroupOrder order);
}
