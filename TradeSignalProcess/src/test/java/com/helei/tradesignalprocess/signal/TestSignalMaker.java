package com.helei.tradesignalprocess.signal;


import com.helei.constants.TradeSide;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.trade.KLine;
import com.helei.tradesignalprocess.stream.c_indicator_signal.maker.AbstractSignalMaker;
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
    protected IndicatorSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
        return IndicatorSignal.builder()
                .name("测试用信号")
                .description("测试用信号")
                .tradeSide(TradeSide.BUY)
                .currentPrice(kLine.getClose())
                .targetPrice(500000.0)
                .stopPrice(0.0)
                .build();
    }

    @Override
    protected IndicatorSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
        return IndicatorSignal.builder().name("测试用信号").tradeSide(TradeSide.BUY).build();
    }
}
