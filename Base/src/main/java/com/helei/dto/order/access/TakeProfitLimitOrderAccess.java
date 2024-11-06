package com.helei.dto.order.access;

import com.helei.constants.order.TimeInForce;

import java.math.BigDecimal;

public interface TakeProfitLimitOrderAccess {


    TimeInForce getTimeInForce();

    void setTimeInForce(TimeInForce timeInForce);

    BigDecimal getPrice();

    void setPrice(BigDecimal price);

    BigDecimal getQuantity();

    void setQuantity(BigDecimal quantity);

    BigDecimal getStopPrice();

    void setStopPrice(BigDecimal stopPrice);

    Integer getTrailingDelta();

    void setTrailingDelta(Integer trailingDelta);
}
