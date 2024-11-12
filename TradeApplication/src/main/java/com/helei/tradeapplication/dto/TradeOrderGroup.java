package com.helei.tradeapplication.dto;


import com.helei.dto.ASKey;
import com.helei.dto.order.CEXTradeOrder;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class TradeOrderGroup {

    /**
     * 请求鉴权信息
     */
    private ASKey asKey;

    /**
     * 主单
     */
    private CEXTradeOrder mainOrder;


    /**
     * 止损单
     */
    private List<CEXTradeOrder> stopOrders;


    /**
     * 止盈单
     */
    private List<CEXTradeOrder> profitOrders;
}
