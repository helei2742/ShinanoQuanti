package com.helei.dto.account;


import com.alibaba.fastjson.JSONObject;
import com.helei.constants.order.PositionSide;
import com.helei.constants.trade.MarginMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionInfo implements Serializable {

    /**
     * 交易对
     */
    private String symbol;
    /**
     * 当前所需起始保证金
     */
    private BigDecimal initialMargin;
    /**
     * 维持保证金
     */
    private BigDecimal maintMargin;
    /**
     * 持仓未实现盈亏
     */
    private BigDecimal unrealizedProfit;
    /**
     * 持仓所需起始保证金
     */
    private BigDecimal positionInitialMargin;
    /**
     * 当前挂单所需起始保证金
     */
    private BigDecimal openOrderInitialMargin;
    /**
     * 杠杆倍率
     */
    private Integer leverage;
    /**
     * 是否是逐仓模式
     */
    private Boolean isolated;
    /**
     * 持仓成本价
     */
    private BigDecimal entryPrice;

    /**
     * 盈亏平衡价格
     */
    private BigDecimal balanceEqualPrice;

    /**
     * 总盈亏
     */
    private Double countProfitOrLoss;
    /**
     * 最大名义价值
     */
    private BigDecimal maxNotional;
    /**
     * 买单净值
     */
    private BigDecimal bidNotional;
    /**
     * 卖单净值
     */
    private BigDecimal askNotional;

    /**
     * 保证金模式
     */
    private MarginMode marginMode;

    /**
     * 持仓方向
     */
    private PositionSide positionSide;
    /**
     * 持仓数量
     */
    private BigDecimal positionAmt;
    /**
     * 更新时间
     */
    private Long updateTime;



    public static PositionInfo fromJson(JSONObject jsonObject) {
        PositionInfo position = new PositionInfo();
        position.setSymbol(jsonObject.getString("symbol"));
        position.setInitialMargin(jsonObject.getBigDecimal("initialMargin"));
        position.setMaintMargin(jsonObject.getBigDecimal("maintMargin"));
        position.setUnrealizedProfit(jsonObject.getBigDecimal("unrealizedProfit"));
        position.setPositionInitialMargin(jsonObject.getBigDecimal("positionInitialMargin"));
        position.setOpenOrderInitialMargin(jsonObject.getBigDecimal("openOrderInitialMargin"));
        position.setLeverage(jsonObject.getIntValue("leverage"));
        position.setIsolated(jsonObject.getBoolean("isolated"));
        position.setEntryPrice(jsonObject.getBigDecimal("entryPrice"));
        position.setMaxNotional(jsonObject.getBigDecimal("maxNotional"));
        position.setBidNotional(jsonObject.getBigDecimal("bidNotional"));
        position.setAskNotional(jsonObject.getBigDecimal("askNotional"));
        position.setPositionSide(PositionSide.valueOf(jsonObject.getString("positionSide")));
        position.setPositionAmt(jsonObject.getBigDecimal("positionAmt"));
        position.setUpdateTime(jsonObject.getLongValue("updateTime"));
        return position;
    }

}
