package com.helei.tradesignalprocess.stream.c_indicator_signal.maker;

import com.helei.constants.TradeSide;
import com.helei.dto.trade.KLine;
import com.helei.dto.trade.IndicatorSignal;
import com.helei.dto.indicator.TrendLine;
import com.helei.dto.indicator.PST;
import com.helei.dto.indicator.config.PSTConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimerService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 盈亏分析器
 */
@Slf4j
public class PSTSignalMaker extends AbstractSignalMaker {

    private final PSTConfig pstConfig;

    private ValueState<PST> curPST;


    /**
     * 止盈和止损位
     */
    private ListState<Double> tsPrice;

    /**
     * 确定向上的价格
     */
    private ValueState<Double> upConfirmPrice;

    /**
     * 确定向下的价格
     */
    private ValueState<Double> downConfirmPrice;

    public PSTSignalMaker(PSTConfig pstConfig) {
        super(true);
        this.pstConfig = pstConfig;
    }


    /**
     * 从k线中获取Pressure  support  trend
     *
     * @param kLine kLine
     * @return PST
     */
    private PST getPST(KLine kLine) {
        return kLine.getIndicators().getIndicator(pstConfig);
    }

    @Override
    public void onOpen(OpenContext openContext) throws Exception {
        curPST = getRuntimeContext().getState(new ValueStateDescriptor<>(pstConfig.getIndicatorName() + "curPST", TypeInformation.of(PST.class)));
        upConfirmPrice = getRuntimeContext().getState(new ValueStateDescriptor<>(pstConfig.getIndicatorName() + "upConfirmPrice", Double.class));
        downConfirmPrice = getRuntimeContext().getState(new ValueStateDescriptor<>(pstConfig.getIndicatorName() + "downConfirmPrice", Double.class));

        tsPrice = getRuntimeContext().getListState(new ListStateDescriptor<>(pstConfig.getIndicatorName() + "tsPrice", Double.class));
    }

    @Override
    protected IndicatorSignal resolveHistoryKLine(KLine kLine, TimerService timerService) throws Exception {
        IndicatorSignal indicatorSignal = tryBuildSignal(kLine, timerService, false);

        PST pst = getPST(kLine);
        curPST.update(pst);
        return indicatorSignal;
    }

    @Override
    protected IndicatorSignal resolveRealTimeKLine(KLine kLine, TimerService timerService) throws Exception {
        return tryBuildSignal(kLine, timerService, true);
    }


    @Override
    protected IndicatorSignal onTimerInvoke() throws Exception {
        KLine kLine = lastHistoryKLine.value();
        if (kLine == null) return null;
        Iterator<Double> iterator = tsPrice.get().iterator();
        if (!iterator.hasNext()) return null;

        Double target = iterator.next();
        Double stop = iterator.next();

        Double upPrice = upConfirmPrice.value();
        //k线结束，还站在上趋势线上
        if (upPrice != null && kLine.getClose() > upPrice) {
            log.info("突破上趋势线，买入信号");
            return buildSignal("突破上趋势线，买入信号", TradeSide.BUY, kLine, target, stop);
        }

        Double downPrice = downConfirmPrice.value();
        //k线结束，还在趋势线下
        if (downPrice != null && kLine.getClose() < downPrice) {
            log.info("跌破下趋势线，卖出信号");
            return buildSignal("跌破下趋势线，卖出信号", TradeSide.SALE, kLine, target, stop);
        }

        return null;
    }

    private IndicatorSignal tryBuildSignal(KLine kLine, TimerService timerService, boolean isCheckEnd) throws Exception {
        PST pst = curPST.value();
        log.debug("当前 PST, symbol [{}] -> [{}]", kLine.getStreamKey(), pst);
        if (pst == null) return null;


        TrendLine upTrendLine = pst.getRelativeUpTrendLine();
        TrendLine downTrendLine = pst.getRelativeDownTrendLine();

        if (upTrendLine == null || downTrendLine == null) {
            log.info("没有完整趋势线，不计算趋势线");
            return null;
        }
        double price = kLine.getClose();


        List<Double> pList = pst.getPressure();
        List<Double> sList = pst.getSupport();

        if (pList.size() < 2 || sList.size() < 2) return null;

        double upK = upTrendLine.getK();
        double downK = downTrendLine.getK();

        double upPrice = upK * (kLine.getCloseTime() / 1000.0 + 1) + upTrendLine.getM();
        double downPrice = downK * (kLine.getCloseTime() / 1000.0 + 1) + downTrendLine.getM();
        if (upK < 0 && downK > 0) { //两条趋势线相交，交点在右侧
            //是否需要检查k线结束才发信号
            if (isCheckEnd) {
                if (price > upPrice) { // 突破上趋势线
                    return buildSignal("两条趋势线相交，交点在右侧，突破上趋势", TradeSide.BUY, kLine, (pList.get(0) + pList.get(1)) / 2, sList.getFirst());
                } else if (price < downPrice) {// 跌破下趋势线
                    return buildSignal("两条趋势线相交，交点在右侧，跌破下趋势线", TradeSide.SALE, kLine, kLine.getOpen() - (pList.getLast() - sList.getLast()), kLine.getHigh());
                } else { //趋势线中间
                    return buildSignal("两条趋势线相交，交点在右侧", TradeSide.SALE, kLine, sList.getFirst(), downPrice);
                }
            } else {
                if (price > upPrice) { // 突破上趋势线
                    upConfirmPrice.update(upPrice);
                    setTimerSignal(kLine, timerService, Arrays.asList((pList.get(0) + pList.get(1)) / 2, sList.getFirst()));
                } else if (price < downPrice) {// 跌破下趋势线
                    downConfirmPrice.update(downPrice);
                    setTimerSignal(kLine, timerService, Arrays.asList(kLine.getOpen() - (pList.getLast() - sList.getLast()), kLine.getHigh()));
                } else { //趋势线中间
                    return buildSignal("两条趋势线相交，交点在右侧", TradeSide.SALE, kLine, sList.getFirst(), downPrice);
                }
            }
        } else if (upK < 0 && downK < 0) { //\\
//            return buildSignal("两条趋势线平行，趋势向下", TradeSide.SALE, kLine, sList.get(0), downPrice);
        } else if (upK > 0 && downK < 0) { //两条趋势线相交，交点左侧
            log.debug("趋势线不符合条件");
            return null;
        } else if (upK > 0 && downK > 0) { //两条趋势线平行，趋势向上
//            return buildSignal("两条趋势线平行，趋势向上",TradeSide.BUY, kLine, upPrice, sList.get(0));
        }

        return null;
    }

    /**
     * 设置定时信号
     *
     * @param kLine        kLine
     * @param timerService timerService
     * @throws Exception Exception
     */

    private void setTimerSignal(KLine kLine, TimerService timerService, List<Double> spList) throws Exception {
        if (!tsPrice.get().iterator().hasNext()) {
            //设置定时执行，看是否还站上
            long invokeTime = kLine.getCloseTime() / 1000 - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            timerService.registerProcessingTimeTimer(invokeTime);
            tsPrice.update(spList);
        }
    }

    private IndicatorSignal buildSignal(String describe, TradeSide side, KLine kLine, Double target, Double stop) {
        return IndicatorSignal
                .builder()
                .name(pstConfig.getIndicatorName())
                .description(describe)
                .targetPrice(target)
                .stopPrice(stop)
                .kLine(kLine)
                .currentPrice(kLine.getClose())
                .tradeSide(side)
                .build();
    }

}
