package com.helei.tradeapplication.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.helei.tradeapplication.dto.GroupOrder;
import com.helei.dto.order.CEXTradeOrder;

import java.util.List;

/**
 * <p>
 * 币安合约交易订单表 服务类
 * </p>
 *
 * @author com.helei
 * @since 2024-11-05
 */
public interface IBinanceContractOrderService extends IService<CEXTradeOrder> {


    /**
     * 保存订单
     * @param order 订单
     */
    List<CEXTradeOrder> saveGroupOrder(GroupOrder order);

}

