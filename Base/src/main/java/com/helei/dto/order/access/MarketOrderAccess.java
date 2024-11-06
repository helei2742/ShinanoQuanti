package com.helei.dto.order.access;

import java.math.BigDecimal;

public interface MarketOrderAccess {

    BigDecimal getQuantity();

    void setQuantity(BigDecimal quantity);

    BigDecimal getQuoteOrderQty();

    void setQuoteOrderQty(BigDecimal quoteOrderQty);
}
