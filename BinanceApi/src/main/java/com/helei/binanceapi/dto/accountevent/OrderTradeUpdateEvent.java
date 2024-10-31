package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import com.helei.binanceapi.constants.order .*;
import com.helei.binanceapi.constants.TimeInForce;
import com.helei.constants.PositionSide;
import com.helei.constants.TradeSide;
import lombok .*;

/**
 * 订单交易更新推送事件
 */
@Getter
@Setter
@ToString

public class OrderTradeUpdateEvent extends AccountEvent {

    /**
     * 撮合时间
     */
    private Long MatchMakingTime;

    /**
     * 发生更新的交易对详情
     */
    private OrderTradeUpdateDetails orderTradeUpdateDetails;

    public OrderTradeUpdateEvent(Long eventTime) {
        super(AccountEventType.ORDER_TRADE_UPDATE, eventTime);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class OrderTradeUpdateDetails {
        /**
         * 交易对
         */
        private String symbol;
        /**
         * 客户端自定订单ID
         */
        private String clientOrderId;
        /**
         * 订单方向
         */
        private TradeSide orderSide;
        /**
         * 订单类型
         */
        private OrderType orderType;
        /**
         * 有效方式
         */
        private TimeInForce timeInForce;
        /**
         * 订单原始数量
         */
        private Double originalQuantity;
        /**
         * 订单原始价格
         */
        private Double originalPrice;
        /**
         * 订单平均价格
         */
        private Double averagePrice;
        /**
         * 条件订单触发价格,对追踪止损单无效
         */
        private String stopPrice;
        /**
         * 本次事件的具体执行类型
         */
        private OrderExcuteType executionType;
        /**
         * 订单的当前状态
         */
        private OrderStatus orderStatus;
        /**
         * 订单ID
         */
        private Long orderId;
        /**
         * 订单末次成交量
         */
        private Double lastFilledQuantity;
        /**
         * 订单累计已成交量
         */
        private Double cumulativeFilledQuantity;
        /**
         * 订单末次成交价格
         */
        private Double lastFilledPrice;
        /**
         * 手续费资产类型
         */
        private String commissionAsset;
        /**
         * 手续费数量
         */
        private Double commissionAmount;
        /**
         * 成交时间
         */
        private Long tradeTime;
        /**
         * 成交ID
         */
        private Long tradeId;
        /**
         * 买单净值
         */
        private Double buyerNetValue;
        /**
         * 卖单净值
         */
        private Double sellerNetValue;
        /**
         * 该成交是作为挂单成交吗？
         */
        private boolean maker;
        /**
         * 是否是只减仓单
         */
        private boolean reduceOnly;
        /**
         * 触发价类型
         */
        private WorkingType workingType;
        /**
         * 原始订单类型
         */
        private OrderType originalOrderType;
        /**
         * 持仓方向
         */
        private PositionSide positionSide;
        /**
         * 是否为触发平仓单,仅在条件订单情况下会推送此字段
         */
        private boolean closePosition;
        /**
         * 追踪止损激活价格,仅在追踪止损单时会推送此字段
         */
        private Double activationPrice;
        /**
         * 追踪止损回调比例,仅在追踪止损单时会推送此字段
         */
        private Double callbackRate;
        /**
         * 是否开启条件单触发保护
         */
        private boolean priceProtect;
        /**
         * 该交易实现盈亏
         */
        private Double realizedProfit;
        /**
         * 自成交防止模式
         */
        private SelfTradePreventionMode selfTradePreventionMode;
        /**
         * 价格匹配模式
         */
        private OpponentPriceMode opponentPriceMode;
        /**
         * TIF为GTD的订单自动取消时间
         */
        private long gtdCancelTime;
    }

}
