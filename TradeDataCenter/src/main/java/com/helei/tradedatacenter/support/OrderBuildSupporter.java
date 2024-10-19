package com.helei.tradedatacenter.support;

import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.tradedatacenter.dto.OriginOrder;

public interface OrderBuildSupporter {

    BaseOrder buildOrder(OriginOrder originOrder, double positionSize);
}
