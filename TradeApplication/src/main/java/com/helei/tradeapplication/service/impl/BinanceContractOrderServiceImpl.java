package com.helei.tradeapplication.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.helei.dto.order.BaseOrder;
import com.helei.dto.order.LimitOrder;
import com.helei.tradeapplication.entity.BinanceContractOrder;
import com.helei.tradeapplication.mapper.BinanceContractOrderMapper;
import com.helei.tradeapplication.service.IBinanceContractOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 币安合约交易订单表 服务实现类
 * </p>
 *
 * @author com.helei
 * @since 2024-11-05
 */
@Slf4j
@Service
public class BinanceContractOrderServiceImpl extends ServiceImpl<BinanceContractOrderMapper, BinanceContractOrder> implements IBinanceContractOrderService {


    @Override
    public void saveOrder(BaseOrder order) {
        BinanceContractOrder saveOrder = switch (order.getOrderType()) {
            case LIMIT -> convertFromLimitOrder(order);
            case MARKET -> null;
            case STOP_MARKET -> null;
            case STOP_LIMIT -> null;
            case TAKE_PROFIT_MARKET -> null;
            case TAKE_PROFIT_LIMIT -> null;
            case TRAILING_STIO_MARKET -> null;
        };

        if (saveOrder == null) {
            log.warn("原始订单[{}]转化为为数据库订单类型后结果为null", order);
            return;
        }

        save(saveOrder);
    }


    /**
     * 将限价单转成插入数据库的订单类型
     *
     * @param order 限价单
     * @return 数据库订单类型
     */
    public static BinanceContractOrder convertFromLimitOrder(BaseOrder order) {
        LimitOrder limitOrder = (LimitOrder) order;
        return BinanceContractOrder
                .builder()
                .userId(limitOrder.getUserId())
                .accountId(limitOrder.getAccountId())
                .symbol(limitOrder.getSymbol())
                .side(limitOrder.getSide().name())
                .positionSide(limitOrder.getPositionSide().name())
                .type(limitOrder.getOrderType().name())
                .quantity(limitOrder.getQuantity())
                .price(limitOrder.getPrice())
                .status(limitOrder.getOrderStatus().name())
                .build();
    }
}


