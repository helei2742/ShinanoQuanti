
package com.helei.tradedatacenter.signal;

import com.helei.tradedatacenter.constants.TradeSide;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.indicator.MACD;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;


public class MACDSignal_V1 extends AbstractSignalMaker{

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
        this.macdName = macdName;
    }


    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<MACD> descriptor = new ValueStateDescriptor<>("lastMACD", TypeInformation.of(MACD.class));
        lastMACD = getRuntimeContext().getState(descriptor);
        underLineGoldAcrossCount = getRuntimeContext().getState(new ValueStateDescriptor<>("underLineGoldAcrossCount", Integer.class));
    }


    @Override
    protected void stateUpdate(KLine kLine) throws IOException {
        MACD lastMACD = this.lastMACD.value();

        MACD currentMACD = (MACD) kLine.getIndicators().get(macdName);


        if (lastMACD != null) {
            int acrossState = calGoldDeathAcross(lastMACD, currentMACD);

            //是否在水下
            boolean under = currentMACD.dif() <= 0;

            //水下金叉
            if (under && acrossState == 1) {
                underLineGoldAcrossCount.update(underLineGoldAcrossCount.value() == null ? 0 : underLineGoldAcrossCount.value() + 1);
            }
        }

        this.lastMACD.update(currentMACD);
    }

    @Override
    protected TradeSignal buildSignal(KLine kLine) throws IOException {
        MACD currentMACD = (MACD) kLine.getIndicators().get(macdName);
        MACD lastMACD = this.lastMACD.value();

        int acrossState = calGoldDeathAcross(lastMACD, currentMACD);
        boolean under = currentMACD.dif() <= 0;

        // 水下第二次金叉，产出买入信号
        if (under && acrossState == 1 && underLineGoldAcrossCount.value() == 1) {

            return TradeSignal
                    .builder()
                    .tradeSide(TradeSide.BUY)
                    .currentPrice(kLine.getClose())
                    .build();
        }

        // 水上死叉，卖出
        if (!under && acrossState == 0) {
            return TradeSignal
                    .builder()
                    .name(macdName)
                    .tradeSide(TradeSide.SALE)
                    .currentPrice(kLine.getClose())
                    .build();
        }

        return null;
    }

    /**
     * 计算金叉死叉
     * @param last  上一次的MACD
     * @param current   当前MACD
     * @return  1 表示金叉， -1 表示死叉， 0 表示没有交叉
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