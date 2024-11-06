package com.helei.dto.order.access;

import com.helei.constants.order.TimeInForce;

import java.math.BigDecimal;

public interface LimitOrderAccess {

    void setTimeInForce(TimeInForce timeInForce);

    TimeInForce getTimeInForce();

    void setPrice(BigDecimal price);

    BigDecimal getPrice();

    BigDecimal getQuantity();

    void setQuantity(BigDecimal quantity);

}
