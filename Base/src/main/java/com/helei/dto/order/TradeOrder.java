package com.helei.dto.order;

import com.helei.constants.order.OrderRespType;
import com.helei.constants.order.TimeInForce;

import java.math.BigDecimal;

@Deprecated
public class TradeOrder {


    /**
     * 有效成交方式
     */
    private TimeInForce timeInForce;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 量
     */
    private BigDecimal quantity;

    /**
     *
     */
    private BigDecimal quoteOrderQty;

    /**
     * 客户自定义的唯一订单ID。如果未发送，则自动生成。
     */
    private String newClientOrderId;

    /**
     * 响应格式
     */
    private OrderRespType newOrderRespType;


    /**
     * 止损
     */
    private BigDecimal stopPrice;

    /**
     * 移动止损
     */
    private Integer trailingDelta;


    private BigDecimal icebergQty;

    /**
     * 标识订单策略中订单的任意ID。
     */
    private Integer strategyId;

    /**
     * 标识订单策略的任意数值。
     * 小于1000000的值是保留的，不能使用。
     */
    private Integer strategyType;


}


