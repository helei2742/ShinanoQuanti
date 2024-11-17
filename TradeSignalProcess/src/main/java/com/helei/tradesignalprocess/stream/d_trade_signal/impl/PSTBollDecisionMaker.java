package com.helei.tradesignalprocess.stream.d_trade_signal.impl;


import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.TradeSignal;
import com.helei.tradesignalprocess.stream.d_trade_signal.BinanceDecisionMaker;
import com.helei.tradesignalprocess.stream.d_trade_signal.config.PSTBollDecisionConfig_v1;
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
public class PSTBollDecisionMaker extends BinanceDecisionMaker {

    private final PSTBollDecisionConfig_v1 config;

    public PSTBollDecisionMaker(PSTBollDecisionConfig_v1 config) {
        super(config.getName());
        this.config = config;
    }

    @Override
    protected List<TradeSignal> makeBinanceTradeSignal(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap) { String pstKey = config.getPstConfig().getIndicatorName();
        String bollKey = config.getBollConfig().getIndicatorName();

        Map<String, List<IndicatorSignal>> signalMap = windowSignal.stream().collect(Collectors.groupingBy(IndicatorSignal::getName));

        List<IndicatorSignal> pstSignals = signalMap.get(pstKey);
        List<IndicatorSignal> bollSignals = signalMap.get(bollKey);

//        if (pstSignals == null || bollSignals == null || pstSignals.isEmpty() || bollSignals.isEmpty()) {
//            log.warn("pst和boll信号不满足共振， 不生成订单");
//            return null;
//        }

        if (bollSignals != null && !signalMap.isEmpty()) {
            IndicatorSignal newBollSignal = bollSignals.getLast();
            //TODO 仅仅测试用
            return List.of(buildMarketOrder(newBollSignal));
        }
//        IndicatorSignal newPstSignal = pstSignals.getLast();


        return null;
    }

    private static TradeSignal buildMarketOrder(IndicatorSignal newBollSignal) {
        return TradeSignal
                .builder()
                .symbol(newBollSignal.getKLine().getSymbol())
                .tradeSide(newBollSignal.getTradeSide())
                .targetPrice(BigDecimal.valueOf(newBollSignal.getTargetPrice()))
                .stopPrice(BigDecimal.valueOf(newBollSignal.getStopPrice()))
                .enterPrice(BigDecimal.valueOf(newBollSignal.getKLine().getCloseTime()))
                .build();
    }
}
