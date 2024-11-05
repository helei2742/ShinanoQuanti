package com.helei.tradeapplication.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.helei.dto.order.BaseOrder;
import com.helei.tradeapplication.entity.BinanceContractOrder;

/**
 * <p>
 * 币安合约交易订单表 服务类
 * </p>
 *
 * @author com.helei
 * @since 2024-11-05
 */
public interface IBinanceContractOrderService extends IService<BinanceContractOrder> {


    /**
     * 保存订单
     * @param order 订单
     */
    void saveOrder(BaseOrder order);

}
