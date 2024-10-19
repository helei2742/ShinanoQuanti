package com.helei.tradedatacenter.support;


import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.cexapi.binanceapi.constants.order.LimitOrder;
import com.helei.cexapi.binanceapi.constants.order.TimeInForce;
import com.helei.tradedatacenter.dto.OriginOrder;

import java.math.BigDecimal;

/**
 * 限价单构建supporter
 */
public class LimitOrderBuildSupporter implements OrderBuildSupporter {

    @Override
    public BaseOrder buildOrder(OriginOrder originOrder, double positionSize) {

        LimitOrder limitOrder = new LimitOrder(TimeInForce.GTC, originOrder.getEnterPrice(), BigDecimal.valueOf(positionSize));
        limitOrder.setSide(originOrder.getTradeSide());
        limitOrder.setSymbol(originOrder.getSymbol());
        return limitOrder;
    }
}
