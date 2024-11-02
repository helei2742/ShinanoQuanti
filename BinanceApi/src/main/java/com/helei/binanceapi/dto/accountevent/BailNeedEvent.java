package com.helei.binanceapi.dto.accountevent;

import com.helei.constants.MarginMode;
import com.helei.constants.PositionSide;
import com.helei.binanceapi.constants.AccountEventType;
import lombok.*;

import java.util.List;

/**
 * 保证金追加通知
 *
 * <p>
 * 当用户持仓风险过高，会推送此消息。
 * 此消息仅作为风险指导信息，不建议用于投资策略。
 * 在大波动市场行情下,不排除此消息发出的同时用户仓位已被强平的可能。
 * 全仓模式下若保证金不足每小时仅会推送一次此事件，不会重复推送；逐仓模式下保证金不足一个交易对每小时仅会推送一次此事件，不会重复推送
 * </p>
 */
@Getter
@Setter
@ToString
public class BailNeedEvent extends AccountEvent {

    /**
     * 除去逐仓仓位保证金的钱包余额, 仅在全仓 margin call 情况下推送此字段
     */
    private Double bailRemoveWalletBalance;

    /**
     * 涉及持仓
     */
    private List<PositionNeedInfo> positionNeedInfos;

    public BailNeedEvent(Long eventTime) {
        super(AccountEventType.MARGIN_CALL, eventTime);
    }


    /**
     * 需要保证金的信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PositionNeedInfo {

        /**
         * 交易对
         */
        private String symbol;

        /**
         * 持仓方向
         */
        private PositionSide positionSide;

        /**
         * 仓位
         */
        private Double position;

        /**
         * 保证金模式
         */
        private MarginMode marginMode;

        /**
         * 保证金
         */
        private Double bail;

        /**
         * 标记价格
         */
        private Double markPrice;

        /**
         * 未实现盈亏
         */
        private Double unrealizedProfitOrLoss;

        /**
         * 持仓需要的维持保证金
         */
        private Double needBail;
    }

}
