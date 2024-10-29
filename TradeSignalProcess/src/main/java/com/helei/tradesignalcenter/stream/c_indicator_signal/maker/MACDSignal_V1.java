package com.helei.tradesignalcenter.stream.c_indicator_signal.maker;

import com.helei.constants.TradeSide;
import com.helei.dto.IndicatorSignal;
import com.helei.dto.KLine;
import com.helei.dto.indicator.MACD;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimerService;

import java.io.IOException;


@Slf4j
public class MACDSignal_V1 extends AbstractSignalMaker {

    private final String macdName;


    /**
     * 上一跟MACD
     */
    private ValueState<MACD> lastMACD;

    /**
     * 水下金叉次数
     */
    private ValueState<Integer> underLineGoldAcrossCount;


    public MACDSignal_V1(String macdName) {
        super(true);
        this.macdName = macdName;
    }


    @Override
    public void onOpen(OpenContext openContext) throws Exception {
        ValueStateDescriptor<MACD> descriptor = new ValueStateDescriptor<>("lastMACD", TypeInformation.of(MACD.class));
        lastMACD = getRuntimeContext().getState(descriptor);

        underLineGoldAcrossCount = getRuntimeContext().getState(new ValueStateDescriptor<>("underLineGoldAcrossCount", Integer.class));

    }


    @Override
    protected IndicatorSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws IOException {
        MACD lastMACD = this.lastMACD.value();

        MACD currentMACD = (MACD) kLine.getIndicators().getIndicator(macdName);


        if (lastMACD != null) {
            //是否在水下
            boolean under = currentMACD.dif() <= 0;
            int acrossState = calGoldDeathAcross(lastMACD, currentMACD);

            //水下金叉
            if (under && acrossState == 1) {
                underLineGoldAcrossCount.update(getUnderGoldAcrossCount() + 1);
            }

            return trySendSignal(kLine, under, acrossState);
        }

        log.info("update macd signal state completed, last MACD[{}], current MACD[{}], underLineGoldAcrossCount[{}]", lastMACD, currentMACD, underLineGoldAcrossCount.value());
        this.lastMACD.update(currentMACD);

        return null;
    }

    @Override
    protected IndicatorSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws IOException {

        MACD currentMACD = (MACD) kLine.getIndicators().getIndicator(macdName);
        MACD lastMACD = this.lastMACD.value();

        if (lastMACD == null) {
            return null;
        }
        int acrossState = calGoldDeathAcross(lastMACD, currentMACD);
        boolean under = currentMACD.dif() <= 0;

        return trySendSignal(kLine, under, acrossState);
    }

    /**
     * 尝试发送信号
     *
     * @param kLine       kLine
     * @param under       是否水下交叉
     * @param acrossState 交叉形态
     * @return 信号，不适合产生信号则返回null
     */
    private IndicatorSignal trySendSignal(KLine kLine, boolean under, int acrossState) throws IOException {
        // 水下第二次金叉，产出买入信号
        if (under && acrossState == 1 && getUnderGoldAcrossCount() == 1) {
            return sendSignal(kLine, TradeSide.BUY);
        }

        // 水上死叉，卖出
        if (!under && acrossState == -1) {
            return sendSignal(kLine, TradeSide.SALE);
        }

        return null;
    }

    /**
     * 尝试发送， 必须满足未发送过信号的条件
     *
     * @return signal
     * @throws IOException IOException
     */
    private IndicatorSignal sendSignal(KLine kLine, TradeSide tradeSide) throws IOException {
        IndicatorSignal signal = IndicatorSignal
                .builder()
                .name(macdName)
                .tradeSide(tradeSide)
                .currentPrice(kLine.getClose())
                .build();
        log.info("macd [{}]信号", signal.getTradeSide());
        return signal;
    }


    private int getUnderGoldAcrossCount() throws IOException {
        return underLineGoldAcrossCount.value() == null ? 0 : underLineGoldAcrossCount.value();
    }


    /**
     * 计算金叉死叉
     *
     * @param last    上一次的MACD
     * @param current 当前MACD
     * @return 1 表示金叉， -1 表示死叉， 0 表示没有交叉
     */
    private int calGoldDeathAcross(MACD last, MACD current) {
        if ((last.dif() > last.getDea()) && (current.dif() <= current.getDea())) {
            return -1;
        }
        if ((last.dif() < last.getDea()) && (current.dif() >= current.getDea())) {
            return 1;
        }
        return 0;
    }
}
