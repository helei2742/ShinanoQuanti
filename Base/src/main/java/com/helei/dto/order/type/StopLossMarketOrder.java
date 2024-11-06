package com.helei.dto.order.type;

import com.helei.constants.order.OrderType;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.dto.order.access.StopLossMarketOrderAccess;

import java.math.BigDecimal;


/**
 * 止损单
 */
public class StopLossMarketOrder extends CEXTradeOrderWrap implements StopLossMarketOrderAccess {

    public StopLossMarketOrder(BaseOrder baseOrder) {
        super(baseOrder, OrderType.STOP_MARKET);
    }


    public BigDecimal getQuantity() {
        return fullFieldOrder.getQuantity();
    }

    public void setQuantity(BigDecimal quantity) {
        fullFieldOrder.setQuantity(quantity);
    }

    public BigDecimal getStopPrice() {
        return fullFieldOrder.getStopPrice();
    }

    public void setStopPrice(BigDecimal stopPrice) {
        fullFieldOrder.setStopPrice(stopPrice);
    }

    public Integer getTrailingDelta() {
        return fullFieldOrder.getTrailingDelta();
    }

    public void setTrailingDelta(Integer trailingDelta) {
        fullFieldOrder.setTrailingDelta(trailingDelta);
    }
}


