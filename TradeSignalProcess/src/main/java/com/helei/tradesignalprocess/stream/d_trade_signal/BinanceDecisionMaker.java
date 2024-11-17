package com.helei.tradesignalprocess.stream.d_trade_signal;

import com.helei.constants.CEXType;
import com.helei.dto.trade.IndicatorMap;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.SignalGroupKey;
import com.helei.dto.trade.TradeSignal;
import com.helei.tradesignalprocess.config.TradeSignalConfig;

import java.util.List;

public abstract class BinanceDecisionMaker extends AbstractDecisionMaker<TradeSignal> {

    private final TradeSignalConfig tradeSignalConfig = TradeSignalConfig.TRADE_SIGNAL_CONFIG;

    protected BinanceDecisionMaker(String name) {
        super(name);
    }


    @Override
    protected List<TradeSignal> decisionAndBuilderTradeSignal(SignalGroupKey signalGroupKey, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap) {
        List<TradeSignal> tradeSignals = makeBinanceTradeSignal(signalGroupKey.getSymbol(), windowSignal, indicatorMap);
        if (tradeSignals == null || tradeSignals.isEmpty()) return null;

        tradeSignals.forEach(tradeSignal -> {
            tradeSignal.setName(getName());
            tradeSignal.setId(nextSignalId());
            tradeSignal.setSymbol(signalGroupKey.getSymbol());
            tradeSignal.setCexType(CEXType.BINANCE);
            tradeSignal.setRunEnv(tradeSignalConfig.getRun_env());
            tradeSignal.setTradeType(tradeSignalConfig.getTrade_type());
            tradeSignal.setCreateTimestamp(System.currentTimeMillis());
            tradeSignal.setCreateKLineOpenTimestamp(signalGroupKey.getOpenTime());
        });

        return tradeSignals;
    }


    protected abstract List<TradeSignal> makeBinanceTradeSignal(String symbol, List<IndicatorSignal> windowSignal, IndicatorMap indicatorMap);

}
