package com.helei.dto.order;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


import com.helei.constants.order.*;
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
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_binance_contract_order")
public class CEXTradeOrder extends BaseOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1231231419839753841L;

    /**
     * 是否仅减少持仓，仅适用于双向持仓模式
     */
    @TableField("reduce_only")
    protected Boolean reduceOnly;

    /**
     * 交易数量
     */
    @TableField("quantity")
    protected BigDecimal quantity;

    /**
     * 订单价格，仅限限价单
     */
    @TableField("price")
    protected BigDecimal price;

    /**
     *
     */
    @TableField("quote_order_qty")
    protected BigDecimal quoteOrderQty;

    /**
     * 移动止损
     */
    protected Integer trailingDelta;


    /**
     * 触发价，仅限触发单
     */
    @TableField("stop_price")
    protected BigDecimal stopPrice;

    /**
     * 是否为全平仓单，仅适用于触发单
     */
    @TableField("close_position")
    protected Boolean closePosition;

    /**
     * 追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
     */
    @TableField("activation_price")
    protected BigDecimal activationPrice;

    /**
     * 追踪止损回调比例，可取值范围[0.1, 10],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
     */
    @TableField("callback_rate")
    protected BigDecimal callbackRate;

    /**
     * 订单有效期类型
     */
    @TableField("time_in_force")
    protected TimeInForce timeInForce;

    /**
     * 触发价格类型
     */
    @TableField("working_type")
    protected WorkingType workingType;

    /**
     * 价格保护开关
     */
    @TableField("price_protect")
    protected Boolean priceProtect;

    /**
     * 响应类型
     */
    @TableField("order_resp_type")
    protected OrderRespType orderRespType;

    /**
     * 不能与price同时传
     */
    @TableField("price_match")
    protected PriceMatch priceMatch;

    /**
     * 防自成交模式， 默认NONE
     */
    @TableField("self_trade_prevention_mode")
    protected SelfTradePreventionMode selfTradePreventionMode;

    /**
     * TIF为GTD时订单的自动取消时间， 当timeInforce为GTD时必传；传入的时间戳仅保留秒级精度，毫秒级部分会被自动忽略，时间戳需大于当前时间+600s且小于253402300799000
     */
    @TableField("good_till_date")
    protected Long goodTillDate;

    /**
     * 请求时间戳
     */
    @TableField("timestamp")
    protected Long timestamp;


    /**
     * 订单编号,交易所给的
     */
    @TableId(value = "order_id", type = IdType.INPUT)
    protected String orderId;


    public CEXTradeOrder(BaseOrder baseOrder) {
        super.setRunEnv(baseOrder.getOriRunEnv());
        super.setTradeType(baseOrder.getOriTradeType());
        super.setCexType(baseOrder.getOriCEXType());

        super.setApiKey(baseOrder.getApiKey());
        super.setSignature(baseOrder.getSignature());
        super.setRecvWindow(baseOrder.getRecvWindow());
        super.setTimestamp(baseOrder.getTimestamp());
        super.setClientOrderId(baseOrder.getClientOrderId());
        super.setUserId(baseOrder.getUserId());
        super.setAccountId(baseOrder.getAccountId());
        super.setSymbol(baseOrder.getSymbol());
        super.setSide(baseOrder.getSide());
        super.setPositionSide(baseOrder.getPositionSide());
        super.setType(baseOrder.getType());
        super.setStatus(baseOrder.getStatus());
        super.setMainOrder(baseOrder.getMainOrder());
        super.setSubOrderIdList(baseOrder.getSubOrderIdList());
        super.setConnectMainOrderId(baseOrder.getConnectMainOrderId());
        super.setCreatedDatetime(baseOrder.getCreatedDatetime());
        super.setUpdatedDatetime(baseOrder.getUpdatedDatetime());
    }
}

