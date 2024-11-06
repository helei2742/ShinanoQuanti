package com.helei.tradeapplication.dto;


import com.helei.constants.order.GroupOrderStatus;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.CEXTradeOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 一组订单，包括开的主单，以及跟随的止损单和止盈单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupOrder {


    /**
     * 主订单
     */
    private BaseOrder mainOrder;

    /**
     * 主订单的止损单列表
     */
    private List<CEXTradeOrderWrap> stopOrders;

    /**
     * 主订单的止盈单列表
     */
    private List<CEXTradeOrderWrap> profitOrders;

    /**
     * 订单组的状态
     */
    private GroupOrderStatus groupOrderStatus;


    /**
     * 为最终存到数据库以及发送到kafka的订单列表，保护mainOrder和所有的stopOrder、profitOrder
     */
    private List<CEXTradeOrder> cexTradeOrders;

    @Override
    public String toString() {
        return "GroupOrder{" +
                "  \nmainOrder=" + mainOrder +
                ", \nstopOrders=" + stopOrders +
                ", \nprofitOrders=" + profitOrders +
                ", \ngroupOrderStatus=" + groupOrderStatus +
                ", \ncexTradeOrders=" + cexTradeOrders +
                '}';
    }
}
