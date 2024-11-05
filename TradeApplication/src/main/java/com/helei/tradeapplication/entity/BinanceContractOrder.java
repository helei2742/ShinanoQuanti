package com.helei.tradeapplication.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

/**
 * <p>
 * 币安合约交易订单表
 * </p>
 *
 * @author com.helei
 * @since 2024-11-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_binance_contract_order")
public class BinanceContractOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1231231419839753841L;

    /**
     * 订单编号, 自定义订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户的账户id
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 交易对，例如 BTCUSDT
     */
    @TableField("symbol")
    private String symbol;

    /**
     * 交易方向
     */
    @TableField("side")
    private String side;

    /**
     * 持仓方向，默认BOTH
     */
    @TableField("position_side")
    private String positionSide;

    /**
     * 订单类型
     */
    @TableField("type")
    private String type;

    /**
     * 是否仅减少持仓，仅适用于双向持仓模式
     */
    @TableField("reduce_only")
    private Boolean reduceOnly;

    /**
     * 交易数量
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 订单价格，仅限限价单
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 用户
     */
    @Deprecated
    @TableField("client_order_id")
    private String clientOrderId;

    /**
     * 触发价，仅限触发单
     */
    @TableField("stop_price")
    private BigDecimal stopPrice;

    /**
     * 是否为全平仓单，仅适用于触发单
     */
    @TableField("close_position")
    private Boolean closePosition;

    /**
     * 追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
     */
    @TableField("activation_price")
    private BigDecimal activationPrice;

    /**
     * 追踪止损回调比例，可取值范围[0.1, 10],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
     */
    @TableField("callback_rate")
    private BigDecimal callbackRate;

    /**
     * 订单有效期类型
     */
    @TableField("time_in_force")
    private String timeInForce;

    /**
     * 触发价格类型
     */
    @TableField("working_type")
    private String workingType;

    /**
     * 价格保护开关
     */
    @TableField("price_protect")
    private Boolean priceProtect;

    /**
     * 响应类型
     */
    @TableField("order_resp_type")
    private String orderRespType;

    /**
     * 不能与price同时传
     */
    @TableField("price_match")
    private String priceMatch;

    /**
     * 防自成交模式， 默认NONE
     */
    @TableField("self_trade_prevention_mode")
    private String selfTradePreventionMode;

    /**
     * TIF为GTD时订单的自动取消时间， 当timeInforce为GTD时必传；传入的时间戳仅保留秒级精度，毫秒级部分会被自动忽略，时间戳需大于当前时间+600s且小于253402300799000
     */
    @TableField("good_till_date")
    private Long goodTillDate;

    /**
     * 请求时间戳
     */
    @TableField("timestamp")
    private Long timestamp;

    /**
     * 订单状态
     */
    @TableField("status")
    private String status;

    /**
     * 订单创建时间
     */
    @TableField(value = "created_datetime", fill = FieldFill.INSERT)
    private LocalDateTime createdDatetime;

    /**
     * 订单更新时间
     */
    @TableField(value = "updated_datetime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedDatetime;
}


