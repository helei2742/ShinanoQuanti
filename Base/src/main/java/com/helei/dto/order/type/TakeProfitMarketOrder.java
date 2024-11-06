package com.helei.dto.order.type;

import com.helei.constants.order.OrderType;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.dto.order.access.TakeProfitMarketOrderAccess;

import java.math.BigDecimal;

/**
 * 止盈单
 */
public class TakeProfitMarketOrder extends CEXTradeOrderWrap implements TakeProfitMarketOrderAccess {

    public TakeProfitMarketOrder(BaseOrder baseOrder) {
        super(baseOrder, OrderType.TAKE_PROFIT_MARKET);
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
