package com.helei.tradedatacenter.support;

import com.helei.binanceapi.dto.order.BaseOrder;
import com.helei.tradedatacenter.dto.OriginOrder;

public interface OrderBuildSupporter {

    BaseOrder buildOrder(OriginOrder originOrder, double positionSize);
}
