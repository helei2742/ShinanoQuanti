package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import lombok.*;

/**
 * 条件订单(TP/SL)触发后拒绝更新推送
 */
@Setter
@Getter
public class ConditionalOrderTriggerRejectEvent extends AccountEvent {

    /**
     * 撮合时间 "T"
     */
    private Long matchMakingTime;

    /**
     * 订单拒绝信息 "or"
     */
    private OrderRejectInfo orderRejectInfo;

    public ConditionalOrderTriggerRejectEvent(Long eventTime) {
        super(AccountEventType.CONDITIONAL_ORDER_TRIGGER_REJECT, eventTime);
    }

    /**
     * 内部类，用于表示订单拒绝信息 "or" 部分
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderRejectInfo {
        /**
         * 交易对 "s"
         */
        private String symbol;
        /**
         * 订单号 "i"
         */
        private Long orderId;
        /**
         * 拒绝原因 "r"
         */
        private String rejectReason;
    }

}
