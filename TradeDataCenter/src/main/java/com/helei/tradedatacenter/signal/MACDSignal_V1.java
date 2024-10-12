
package com.helei.tradedatacenter.signal;

import cn.hutool.core.util.BooleanUtil;
import com.helei.tradedatacenter.constants.TradeSide;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.entity.TradeSignal;
import com.helei.tradedatacenter.indicator.MACD;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;


@Slf4j
public class MACDSignal_V1 extends AbstractSignalMaker{

    private final String macdName;


    /**
     * 当前k线，就是buildSignal(kline) 参数kline同意openTime的k线
     */
    private ValueState<KLine> curKLine;

    /**
     * 当前是否发出过信号
     */
    private ValueState<Boolean> isCurSendSignal;

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

        curKLine = getRuntimeContext().getState(new ValueStateDescriptor<>("currentKLine", TypeInformation.of(KLine.class)));

        isCurSendSignal = getRuntimeContext().getState(new ValueStateDescriptor<>("isCurSendSignal", Boolean.class));
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
                underLineGoldAcrossCount.update(getUnderGoldAcrossCount() + 1);
            }
        }

        log.info("update macd signal state completed, last MACD[{}], current MACD[{}], underLineGoldAcrossCount[{}]", lastMACD, currentMACD, underLineGoldAcrossCount.value());
        this.lastMACD.update(currentMACD);
    }

    @Override
    protected TradeSignal buildSignal(KLine kLine) throws IOException {



        MACD currentMACD = (MACD) kLine.getIndicators().get(macdName);
        MACD lastMACD = this.lastMACD.value();

        if (lastMACD == null) {
            return null;
        }
        int acrossState = calGoldDeathAcross(lastMACD, currentMACD);
        boolean under = currentMACD.dif() <= 0;

        // 水下第二次金叉，产出买入信号
        if (under && acrossState == 1 && getUnderGoldAcrossCount() == 1) {
            return trySendSignal(TradeSignal
                    .builder()
                    .name(macdName)
                    .tradeSide(TradeSide.BUY)
                    .currentPrice(kLine.getClose())
                    .build());
        }

        // 水上死叉，卖出
        if (!under && acrossState == -1) {
            return trySendSignal(TradeSignal
                    .builder()
                    .name(macdName)
                    .tradeSide(TradeSide.SALE)
                    .currentPrice(kLine.getClose())
                    .build());
        }

        updateCurKLine(kLine);
        return null;
    }

    /**
     * 尝试发送， 必须满足未发送过信号的条件
     * @param signal signal
     * @return signal
     * @throws IOException IOException
     */
    private TradeSignal trySendSignal(TradeSignal signal) throws IOException {

        //1.发送过信号，发送null
        if (BooleanUtil.isTrue(isCurSendSignal.value())) {
            log.info("已发送过macd信号");
            return null;
        }
        isCurSendSignal.update(true);
        log.info("macd [{}]信号", signal.getTradeSide());
        return signal;
    }

    private int getUnderGoldAcrossCount() throws IOException {
        return underLineGoldAcrossCount.value() == null ? 0 : underLineGoldAcrossCount.value();
    }

    /**
     * 更新当前k线，如果成功更新，还要将isSendSignal设置为false
     * @param cur cur
     * @throws IOException IOException
     */
    private void updateCurKLine(KLine cur) throws IOException {
        KLine last = curKLine.value();
        if (last == null || !last.getOpenTime().isEqual(cur.getOpenTime())) {
            curKLine.update(cur);
            isCurSendSignal.update(false );
        }
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