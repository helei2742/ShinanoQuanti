package com.helei.dto.order.type;

import com.helei.constants.order.OrderType;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.dto.order.access.MarketOrderAccess;

import java.math.BigDecimal;

/**
 * 市价单
 */
public class MarketOrder extends CEXTradeOrderWrap implements MarketOrderAccess {
    public MarketOrder(BaseOrder baseOrder) {
        super(baseOrder, OrderType.MARKET);
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
    public BigDecimal getQuoteOrderQty() {
        return fullFieldOrder.getQuoteOrderQty();
    }

    @Override
    public void setQuoteOrderQty(BigDecimal quoteOrderQty) {
        fullFieldOrder.setQuoteOrderQty(quoteOrderQty);
    }
}
