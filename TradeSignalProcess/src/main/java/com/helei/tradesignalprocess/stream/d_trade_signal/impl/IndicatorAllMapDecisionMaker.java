package com.helei.tradesignalprocess.stream.d_trade_signal.impl;

import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.TradeSignal;
import com.helei.tradesignalprocess.stream.d_trade_signal.BinanceDecisionMaker;

import java.math.BigDecimal;
import java.util.List;


public class IndicatorAllMapDecisionMaker extends BinanceDecisionMaker {

    public IndicatorAllMapDecisionMaker() {
        super("指标信号全量映射交易信号处理器");
    }

    @Override
    protected List<TradeSignal> makeBinanceTradeSignal(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap) {
        return windowSignal.stream().map(IndicatorAllMapDecisionMaker::buildMarketOrder).toList();
    }


    private static TradeSignal buildMarketOrder(IndicatorSignal newBollSignal) {
        return TradeSignal
                .builder()
                .symbol(newBollSignal.getKLine().getSymbol())
                .tradeSide(newBollSignal.getTradeSide())
                .targetPrice(BigDecimal.valueOf(newBollSignal.getTargetPrice()))
                .stopPrice(BigDecimal.valueOf(newBollSignal.getStopPrice()))
                .enterPrice(BigDecimal.valueOf(newBollSignal.getKLine().getClose()))
                .build();
    }
}
