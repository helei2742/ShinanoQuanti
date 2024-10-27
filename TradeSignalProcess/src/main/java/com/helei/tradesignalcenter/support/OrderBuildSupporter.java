package com.helei.tradesignalcenter.support;

import com.helei.binanceapi.dto.order.BaseOrder;
import com.helei.tradesignalcenter.dto.OriginOrder;

public interface OrderBuildSupporter {

    BaseOrder buildOrder(OriginOrder originOrder, double positionSize);
}
