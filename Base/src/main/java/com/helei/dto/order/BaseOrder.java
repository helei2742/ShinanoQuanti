package com.helei.dto.order;

import com.helei.constants.*;
import com.helei.constants.order.OrderStatus;
import com.helei.constants.order.OrderType;
import com.helei.constants.order.PositionSide;
import com.helei.constants.trade.TradeSide;
import com.helei.constants.trade.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class BaseOrder {

    /**
     * 订单类型
     */
    private final OrderType orderType;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 交易方向
     */
    private TradeSide side;

    /**
     * apikey
     */
    private String apiKey;

    /**
     * 签名
     */
    private String signature;

    private Long recvWindow;

    private Long timestamp;


    /**
     * 持仓方向
     */
    private PositionSide positionSide;

    /**
     * 运行环境
     */
    private RunEnv runEnv;

    /**
     * 交易类型
     */
    private TradeType tradeType;

    /**
     * 交易所类型
     */
    private CEXType cexType = CEXType.BINANCE;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 用户的账户id
     */
    private long accountId;

    /**
     * 订单状态
     */
    private OrderStatus orderStatus;


    public BaseOrder(OrderType orderType) {
        this.orderType = orderType;
    }
}
