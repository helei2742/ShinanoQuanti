package com.helei.realtimedatacenter.dto;

import com.helei.constants.trade.KLineInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 交易对下k线总类信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SymbolKLineInfo {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 交易对下的k线频率
     */
    private Set<KLineInterval> intervals;
}
