package com.helei.tradeapplication.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.helei.dto.order.CEXTradeOrder;
import com.helei.dto.order.CEXTradeOrderWrap;
import com.helei.tradeapplication.dto.GroupOrder;
import com.helei.tradeapplication.mapper.TradeOrderMapper;
import com.helei.tradeapplication.service.ITradeOrderJDBCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
public class TradeOrderJDBCServiceImpl extends ServiceImpl<TradeOrderMapper, CEXTradeOrder> implements ITradeOrderJDBCService {


    @Transactional
    @Override
    public List<CEXTradeOrder> saveGroupOrder(GroupOrder groupOrder) {

        List<CEXTradeOrder> orderList = new ArrayList<>();

        // 主单
        orderList.add((CEXTradeOrder) groupOrder.getMainOrder());

        // 止损单
        List<CEXTradeOrderWrap> stopOrders = groupOrder.getStopOrders();
        if (stopOrders != null) {
            for (CEXTradeOrderWrap stopOrder : stopOrders) {
                orderList.add(stopOrder.getFullFieldOrder());
            }
        }

        //止盈单
        List<CEXTradeOrderWrap> profitOrders = groupOrder.getProfitOrders();
        if (profitOrders != null) {
            for (CEXTradeOrderWrap profitOrder : profitOrders) {
                orderList.add(profitOrder.getFullFieldOrder());
            }
        }

        //保存到数据库
        saveBatch(orderList);

        return orderList;
    }
}

