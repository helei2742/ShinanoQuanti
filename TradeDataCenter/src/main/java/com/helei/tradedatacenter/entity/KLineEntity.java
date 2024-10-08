package com.helei.tradedatacenter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * K线实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class KLineEntity {

    /**
     * 开盘价格
     */
    private Double open;

    /**
     * 收盘价格
     */
    private Double close;

    /**
     * 最高价格
     */
    private Double high;

    /**
     * 最低价格
     */
    private Double low;

    /**
     * 成交量
     */
    private Double volume;

    /**
     * 开盘时间
     */
    private LocalDateTime openTime;

    /**
     * 收盘时间
     */
    private LocalDateTime closeTime;
}
