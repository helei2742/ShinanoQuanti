package com.helei.tradesignalprocess.stream.d_trade_signal.config;


import com.helei.dto.indicator.config.BollConfig;
import com.helei.dto.indicator.config.PSTConfig;
import lombok.*;

/**
 * boll决策配置
 */
@Getter
public class PSTBollDecisionConfig_v1 extends DecisionConfig {

    private PSTConfig pstConfig;

    private BollConfig bollConfig;

    public PSTBollDecisionConfig_v1() {
        super("趋势线结合Boll信号决策");
    }

    public PSTBollDecisionConfig_v1(PSTConfig pstConfig, BollConfig bollConfig) {
        super("趋势线结合Boll信号决策");
        this.pstConfig = pstConfig;
        this.bollConfig = bollConfig;
    }

}
