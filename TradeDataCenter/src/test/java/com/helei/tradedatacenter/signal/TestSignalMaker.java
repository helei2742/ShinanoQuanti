package com.helei.tradedatacenter.signal;


import com.helei.cexapi.binanceapi.constants.order.TradeSide;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.resolvestream.signal.AbstractSignalMaker;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.streaming.api.TimerService;

public class TestSignalMaker extends AbstractSignalMaker {
    public TestSignalMaker() {
        super(true);
    }

    @Override
    public void onOpen(OpenContext openContext) throws Exception {

    }

    @Override
    protected TradeSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
        return TradeSignal.builder()
                .name("测试用信号")
                .description("测试用信号")
                .tradeSide(TradeSide.BUY)
                .currentPrice(kLine.getClose())
                .kLine(kLine)
                .targetPrice(500000.0)
                .stopPrice(0.0)
                .build();
    }

    @Override
    protected TradeSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
        return TradeSignal.builder().name("测试用信号").tradeSide(TradeSide.BUY).build();
    }
}