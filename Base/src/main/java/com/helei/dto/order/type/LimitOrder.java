package com.helei.dto.order.type;

import com.helei.constants.order.OrderType;
import com.helei.constants.order.TimeInForce;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.dto.order.access.LimitOrderAccess;

import java.math.BigDecimal;

/**
 * 限价单
 */
public class LimitOrder extends CEXTradeOrderWrap implements LimitOrderAccess {


    public LimitOrder(BaseOrder baseOrder) {
        super(baseOrder, OrderType.LIMIT);
    }

    @Override
    public void setTimeInForce(TimeInForce timeInForce) {
        fullFieldOrder.setTimeInForce(timeInForce);
    }

    @Override
    public TimeInForce getTimeInForce() {
        return fullFieldOrder.getTimeInForce();
    }

    @Override
    public void setPrice(BigDecimal price) {
        fullFieldOrder.setPrice(price);
    }

    @Override
    public BigDecimal getPrice() {
        return fullFieldOrder.getPrice();
    }

    @Override
    public BigDecimal getQuantity() {
        return fullFieldOrder.getQuantity();
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        fullFieldOrder.setQuantity(quantity);
    }
}
