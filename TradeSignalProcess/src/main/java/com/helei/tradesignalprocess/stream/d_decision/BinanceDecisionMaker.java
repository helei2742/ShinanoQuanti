package com.helei.tradesignalprocess.stream.d_decision;

import com.helei.constants.CEXType;
import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.TradeSignal;
import com.helei.tradesignalprocess.config.TradeSignalConfig;

import java.util.List;

public abstract class BinanceDecisionMaker extends AbstractDecisionMaker<TradeSignal> {

    private final TradeSignalConfig tradeSignalConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG;

    protected BinanceDecisionMaker(String name) {
        super(name);
    }


    @Override
    protected TradeSignal decisionAndBuilderOrder(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap) {
        TradeSignal tradeSignal = makeBinanceTradeSignal(symbol, windowSignal, indicatorMap);
        tradeSignal.setSymbol(symbol);
        tradeSignal.setCexType(CEXType.BINANCE);
        tradeSignal.setRunEnv(tradeSignalConfig.getRun_env());
        tradeSignal.setTradeType(tradeSignalConfig.getTrade_type());
        tradeSignal.setCreateTimestamp(System.currentTimeMillis());

        return tradeSignal;
    }


    protected abstract TradeSignal makeBinanceTradeSignal(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap);

}
