package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import com.helei.constants.TradeSide;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 精简交易推送相比原有的ORDER_TRADE_UPDATE流减少了数据延迟，但该交易推送仅推送和交易相关的字段。
 */
@Setter
@Getter
@ToString

public class OrderTradeUpdateLiteEvent extends AccountEvent {

    /**
     * 交易时间 "T"
     */
    private Long tradeTime;
    /**
     * 交易对 "s"
     */
    private String symbol;
    /**
     * 订单原始数量 "q" (用 double 表示)
     */
    private Double originalQuantity; //
    /**
     * 订单原始价格 "p" (用 double 表示)
     */
    private Double originalPrice;
    /**
     * 该成交是作为挂单成交吗？"m"
     */
    private Boolean isMaker;
    /**
     * 客户端自定义订单ID "c"
     */
    private String clientOrderId;
    /**
     * 订单方向 "S"
     */
    private TradeSide orderSide;
    /**
     * 订单末次成交价格 "L" (用 double 表示)
     */
    private Double lastTradePrice;
    /**
     * 订单末次成交量 "l" (用 double 表示)
     */
    private Double lastTradeQuantity;
    /**
     * 成交ID "t"
     */
    private Long tradeId;
    /**
     * 订单ID "i"
     */
    private Long orderId;


    public OrderTradeUpdateLiteEvent(Long eventTime) {
        super(AccountEventType.ORDER_TRADE_UPDATE_LITE, eventTime);
    }
}
