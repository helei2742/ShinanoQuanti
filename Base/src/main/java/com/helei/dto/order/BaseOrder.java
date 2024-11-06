package com.helei.dto.order;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.helei.constants.*;
import com.helei.constants.order.OrderStatus;
import com.helei.constants.order.OrderType;
import com.helei.constants.order.PositionSide;
import com.helei.constants.trade.TradeSide;
import com.helei.constants.trade.TradeType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class BaseOrder {

    /**
     * 订单编号, 自定义订单ID
     */
    @TableId(value = "order_id", type = IdType.INPUT)
    protected String orderId;
    /**
     * 用户id
     */
    @TableField("user_id")
    protected Long userId;

    /**
     * 用户的账户id
     */
    @TableField("account_id")
    protected Long accountId;

    /**
     * 交易对，例如 BTCUSDT
     */
    @TableField("symbol")
    protected String symbol;

    /**
     * 交易方向
     */
    @TableField("side")
    protected TradeSide side;

    /**
     * 持仓方向，默认BOTH
     */
    @TableField("position_side")
    protected PositionSide positionSide;


    /**
     * 运行环境
     */
    protected RunEnv runEnv;

    /**
     * 交易类型
     */
    protected TradeType tradeType;

    /**
     * 交易所类型
     */
    protected CEXType cexType = CEXType.BINANCE;


//    ===================================  下面是基础订单不用立刻写的数据 ======================================


    /**
     * 订单类型
     */
    @TableField("type")
    protected OrderType type;

    /**
     * 订单状态
     */
    @TableField("status")
    protected OrderStatus status;

    /**
     * 是否是主订单
     */
    @TableField("main_order")
    protected Boolean mainOrder;

    /**
     * 子订单的id列表，用’,‘号隔开
     */
    @TableField("sub_order_Id_list")
    protected String subOrderIdList;

    /**
     * 如果是辅助单，则必填的关联的主单id
     */
    @TableField("connect_main_order_id")
    protected Long connectMainOrderId;

    /**
     * 订单创建时间
     */
    @TableField(value = "created_datetime", fill = FieldFill.INSERT)
    protected LocalDateTime createdDatetime;

    /**
     * 订单更新时间
     */
    @TableField(value = "updated_datetime", fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updatedDatetime;


    /**
     * apikey
     */
    protected String apiKey;

    /**
     * 签名
     */
    protected String signature;

    /**
     * 交易所接口参数
     */
    protected Long recvWindow;

    /**
     * 交易所接口参数
     */
    protected Long timestamp;


}


