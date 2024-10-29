package com.helei.tradesignalcenter.stream.d_decision;


import com.helei.dto.IndicatorMap;
import com.helei.dto.IndicatorSignal;
import com.helei.tradesignalcenter.dto.TradeSignal;
import com.helei.tradesignalcenter.stream.d_decision.config.PSTBollDecisionConfig_v1;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 根据PST和Boll指标决策下单
 */
@Deprecated
@Slf4j
public class PSTBollDecisionMaker extends AbstractDecisionMaker<TradeSignal> {

    private final PSTBollDecisionConfig_v1 config;

    public PSTBollDecisionMaker(PSTBollDecisionConfig_v1 config) {
        super(config.getName());
        this.config = config;
    }

    @Override
    protected TradeSignal decisionAndBuilderOrder(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap) {
        String pstKey = config.getPstConfig().getIndicatorName();
        String bollKey = config.getBollConfig().getIndicatorName();

        Map<String, List<IndicatorSignal>> signalMap = windowSignal.stream().collect(Collectors.groupingBy(IndicatorSignal::getName));

        List<IndicatorSignal> pstSignals = signalMap.get(pstKey);
        List<IndicatorSignal> bollSignals = signalMap.get(bollKey);

        if (pstSignals == null || bollSignals == null || pstSignals.isEmpty() || bollSignals.isEmpty()) {
            log.warn("pst和boll信号不满足共振， 不生成订单");
            return null;
        }

        IndicatorSignal newPstSignal = pstSignals.getLast();
        IndicatorSignal newBollSignal = bollSignals.getLast();

        //TODO 仅仅测试用
        return buildMarketOrder(newBollSignal);
    }

    private static TradeSignal buildMarketOrder(IndicatorSignal newBollSignal) {
        return TradeSignal
                .builder()
                .symbol(newBollSignal.getKLine().getSymbol())
                .tradeSide(newBollSignal.getTradeSide())
                .targetPrice(BigDecimal.valueOf(newBollSignal.getTargetPrice()))
                .stopPrice(BigDecimal.valueOf(newBollSignal.getStopPrice()))
                .build();
    }
}
