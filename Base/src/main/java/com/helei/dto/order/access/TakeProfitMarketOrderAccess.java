package com.helei.dto.order.access;

import java.math.BigDecimal;

public interface TakeProfitMarketOrderAccess {

    BigDecimal getQuantity();

    void setQuantity(BigDecimal quantity);

    BigDecimal getStopPrice();

    void setStopPrice(BigDecimal stopPrice);

    Integer getTrailingDelta();

    void setTrailingDelta(Integer trailingDelta);

}

