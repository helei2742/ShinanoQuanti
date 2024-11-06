package com.helei.dto.order;

import com.helei.constants.order.OrderType;
import lombok.Getter;

/**
 * CEXTradeOrder 的包装类
 */
@Getter
public abstract class CEXTradeOrderWrap {

    /**
     * -- GETTER --
     *  获取完整订单
     */
    protected final CEXTradeOrder fullFieldOrder;

    public CEXTradeOrderWrap(BaseOrder baseOrder, OrderType type) {
        this.fullFieldOrder = new CEXTradeOrder(baseOrder);
        this.fullFieldOrder.setType(type);
    }
}
