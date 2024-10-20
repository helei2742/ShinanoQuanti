package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.order.PositionSide;
import com.helei.binanceapi.constants.AccountEventType;
import lombok.*;

import java.util.List;

/**
 * 保证金追加通知
 */
@Getter
@Setter
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
         * TODO 枚举
         */
        private String mode;

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
