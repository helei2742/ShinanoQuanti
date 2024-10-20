package com.helei.binanceapi.dto.accountevent;

import com.helei.binanceapi.constants.AccountEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 杠杆倍数等账户配置 更新推送
 */
public class AccountConfigUpdateEvent extends AccountEvent {

    /**
     * 撮合时间
     */
    private Long matchMakingTime;

    /**
     * 账户杠杠信息变化信息
     */
    private AccountLeverConfigChangeInfo leverChangeInfo;

    /**
     * 账户联合保证变化信息
     */
    private AccountInfoChangeInfo infoChangeInfo;

    public AccountConfigUpdateEvent(Long eventTime) {
        super(AccountEventType.ACCOUNT_CONFIG_UPDATE, eventTime);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountLeverConfigChangeInfo {
        /**
         * 交易对
         */
        private String symbol;

        /**
         * 杠杠倍数
         */
        private int lever;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountInfoChangeInfo {
        /**
         * 联合保证金状态
         */
        private boolean unitBailState;
    }
}
