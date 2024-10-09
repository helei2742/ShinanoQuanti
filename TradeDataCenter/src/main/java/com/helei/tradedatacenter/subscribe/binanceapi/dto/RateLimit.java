package com.helei.tradedatacenter.subscribe.binanceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class RateLimit{
    /**
     * 	频率限制类型: REQUEST_WEIGHT, ORDERS
     */
    private String rateLimitType;

    /**
     * 	频率限制间隔: SECOND, MINUTE, HOUR, DAY
     */
    private String interval;

    /**
     * 频率限制间隔乘数
     */
    private Integer intervalNum;

    /**
     * 每个间隔的请求限制
     */
    private Integer limit;

    /**
     * 	每个间隔的当前使用情况
     */
    private Integer count;
}