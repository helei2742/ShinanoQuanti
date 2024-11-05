package com.helei.dto.account;


import com.helei.constants.order.PositionSide;
import com.helei.constants.trade.MarginMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionInfo implements Serializable {


    /**
     * 交易对
     */
    private String symbol;

    /**
     * 仓位
     */
    private Double position;

    /**
     * 入仓价格
     */
    private Double enterPosition;

    /**
     * 盈亏平衡价
     */
    private Double balanceEqualPrice;

    /**
     * 总盈亏
     */
    private Double countProfitOrLoss;

    /**
     * 未实现盈亏
     */
    private Double unrealizedProfitOrLoss;

    /**
     * 保证金模式
     */
    private MarginMode marginMode;

    /**
     * 保证金
     */
    private Double bail;

    /**
     * 持仓方向
     */
    private PositionSide positionSide;


}
