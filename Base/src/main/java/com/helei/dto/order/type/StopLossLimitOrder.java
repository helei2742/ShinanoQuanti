package com.helei.dto.order.type;

import com.helei.constants.order.TimeInForce;
import com.helei.constants.order.OrderType;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.dto.order.access.StopLossLimitOrderAccess;

import java.math.BigDecimal;

/**
 * 限价止损单
 */
public class StopLossLimitOrder extends CEXTradeOrderWrap implements StopLossLimitOrderAccess {

    public StopLossLimitOrder(BaseOrder baseOrder) {
        super(baseOrder, OrderType.STOP_LIMIT);
    }


    @Override
    public TimeInForce getTimeInForce() {
        return fullFieldOrder.getTimeInForce();
    }

    @Override
    public void setTimeInForce(TimeInForce timeInForce) {
        fullFieldOrder.setTimeInForce(timeInForce);
    }

    @Override
    public BigDecimal getPrice() {
        return fullFieldOrder.getPrice();
    }

    @Override
    public void setPrice(BigDecimal price) {
        fullFieldOrder.setPrice(price);
    }

    @Override
    public BigDecimal getQuantity() {
        return fullFieldOrder.getQuantity();
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        fullFieldOrder.setQuantity(quantity);
    }

    @Override
    public BigDecimal getStopPrice() {
        return fullFieldOrder.getStopPrice();
    }

    @Override
    public void setStopPrice(BigDecimal stopPrice) {
        fullFieldOrder.setStopPrice(stopPrice);
    }

    @Override
    public Integer getTrailingDelta() {
        return fullFieldOrder.getTrailingDelta();
    }

    @Override
    public void setTrailingDelta(Integer trailingDelta) {
        fullFieldOrder.setTrailingDelta(trailingDelta);
    }

}

