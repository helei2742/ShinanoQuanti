package com.helei.tradesignalcenter.resolvestream.c_signal.maker;

import com.helei.constants.TradeSide;
import com.helei.dto.KLine;
import com.helei.dto.TradeSignal;
import com.helei.dto.indicator.Boll;
import com.helei.dto.indicator.config.BollConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.streaming.api.TimerService;

import java.io.IOException;

@Slf4j
public class BollSignalMaker extends AbstractSignalMaker {

    private final BollConfig bollConfig;


    public BollSignalMaker(BollConfig bollConfig) {
        super(true);
        this.bollConfig = bollConfig;
    }

    @Override
    public void onOpen(OpenContext openContext) throws Exception {
    }


    @Override
    protected TradeSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws IOException {
        TradeSignal tradeSignal = tryBuildUpDownSignal(kLine);
        return tradeSignal == null ? tryBuildCenterSignal(kLine) : tradeSignal;
    }

    @Override
    protected TradeSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws IOException {
        return tryBuildUpDownSignal(kLine);
    }

    /**
     * 上下轨信号
     *
     * @param kLine kLine
     * @return TradeSignal
     */
    private TradeSignal tryBuildUpDownSignal(KLine kLine) throws IOException {

        Boll boll = kLine.getIndicator(bollConfig);
        if (boll == null) return null;

        double price = kLine.getClose();
        Double upper = boll.getUpper();
        Double lower = boll.getLower();
        Double sma = boll.getSma();

        //1.上下轨信号
        if (price >= upper) { //触碰boll上轨，卖出信号
            return buildSignal(kLine, TradeSide.SALE, "触碰boll上轨", sma, kLine.getHigh());
        }
        if (price <= lower) { //触碰boll下轨，买入信号
            return buildSignal(kLine, TradeSide.BUY, "触碰boll下轨", sma, kLine.getLow());
        }

        return null;
    }

    /**
     * 中轨穿越信号
     *
     * @param kLine kLine
     * @return TradeSignal
     */
    private TradeSignal tryBuildCenterSignal(KLine kLine) {
        Boll curBoll = kLine.getIndicator(bollConfig);
        if (curBoll == null) return null;

        if (kLine.getOpen() < curBoll.getSma() && kLine.getClose() > curBoll.getSma()) { //上穿中轨
            return buildSignal(kLine, TradeSide.BUY, "上穿中轨", curBoll.getUpper(), kLine.getLow());
        }

        if (kLine.getOpen() > curBoll.getSma() && kLine.getClose() < curBoll.getSma()) {
            return buildSignal(kLine, TradeSide.SALE, "下穿中轨", curBoll.getLower(), kLine.getHigh());
        }

        return null;
    }

    private TradeSignal buildSignal(KLine kLine, TradeSide tradeSide, String description, double target, double stop) {
        return TradeSignal.builder()
                .name(bollConfig.getIndicatorName())
                .description(description)
                .tradeSide(tradeSide)
                .currentPrice(kLine.getClose())
                .kLine(kLine)
                .targetPrice(target)
                .stopPrice(stop)
                .build();
    }
}
