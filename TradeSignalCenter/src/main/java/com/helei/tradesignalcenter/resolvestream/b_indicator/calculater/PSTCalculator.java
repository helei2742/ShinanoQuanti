package com.helei.tradesignalcenter.resolvestream.b_indicator.calculater;

import cn.hutool.core.util.BooleanUtil;
import com.helei.dto.TrendLine;
import com.helei.dto.KLine;
import com.helei.dto.indicator.PST;
import com.helei.dto.indicator.config.PSTConfig;
import com.helei.util.CalculatorUtil;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;


import java.util.*;

/**
 * 计算压力线，支撑线，趋势线
 */
public class PSTCalculator extends BaseIndicatorCalculator<PST> {

    private final int pCount;

    private final int sCount;

    private final int windowLength;

    private ListState<KLine> windowDataState;

    public PSTCalculator(PSTConfig pstConfig) {
        super(pstConfig);
        this.windowLength = pstConfig.getWindowLength();
        this.pCount = pstConfig.getPressureCount();
        this.sCount = pstConfig.getSupportCount();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.windowDataState = getRuntimeContext().getListState(new ListStateDescriptor<>(indicatorConfig.getIndicatorName() + "-WindowData", TypeInformation.of(KLine.class)));
    }

    @Override
    public PST calculateInKLine(KLine kLine) throws Exception {
        if (BooleanUtil.isFalse(kLine.isEnd())) return null;


        LinkedList<KLine> priceList = new LinkedList<>();

        for (KLine line : windowDataState.get()) {
            priceList.add(line);
        }
        priceList.add(kLine);
        while (priceList.size() > windowLength) {
            priceList.remove(0);
        }

        windowDataState.update(priceList);

        if (priceList.size() < 3) {
            return null;
        }

        // 相对高点
        List<KLine> high = new ArrayList<>();
        // 相对低点
        List<KLine> low = new ArrayList<>();
        //压力
        List<Double> pList = new ArrayList<>();
        //支持
        List<Double> sList = new ArrayList<>();


        int[] mmIdx = calRelativeHL(priceList, high, low, pList, sList);

        return calPST(priceList, high, low, pList, sList, mmIdx);
    }

    private PST calPST(LinkedList<KLine> priceList, List<KLine> high, List<KLine> low, List<Double> pList, List<Double> sList, int[] mmIdx) {
        int maxIdx = mmIdx[0];
        int minIdx = mmIdx[1];

        TrendLine upTrendLine = CalculatorUtil.calculateTrend(
                CalculatorUtil.getLastMonotonicPart(high, KLine::getHigh),
                k -> (double) (k.getOpenTime() / 1000),
                KLine::getHigh
        );
        TrendLine downTrendLine = CalculatorUtil.calculateTrend(
                CalculatorUtil.getLastMonotonicPart(low, KLine::getLow),
                k -> (double) (k.getOpenTime() / 1000),
                KLine::getLow
        );

        return PST
                .builder()
                .pressure(new ArrayList<>(pList.subList(0, Math.max(0,  Math.min(pList.size()-1, pCount)))))
                .support(new ArrayList<>(sList.subList(0, Math.max(0, Math.min(sList.size()-1, sCount)))))
                .relativeUpTrendLine(upTrendLine)
                .relativeDownTrendLine(downTrendLine)
                .maxPrice(priceList.get(maxIdx).getHigh())
                .minPrice(priceList.get(minIdx).getLow())
                .build();
    }



    private int[] calRelativeHL(LinkedList<KLine> priceList, List<KLine> high, List<KLine> low, List<Double> pList, List<Double> sList) {
        int maxIdx = 0;
        int minIdx = 0;

        KLine pre = priceList.get(0);
        KLine cur = priceList.get(1);
        //取趋势线,
        for (int i = 3; i < priceList.size(); i++) {
            KLine next = priceList.get(i);
            //相对低点
            if (cur.getLow() < next.getLow() && pre.getLow() > cur.getLow()) {
                low.add(cur);
                //添加压力线，为前一个的相对高点
                if (!high.isEmpty()) {
                    pList.add(high.get(high.size() - 1).getHigh());
                }
            }

            //相对高点
            if (cur.getHigh() > next.getClose() && pre.getHigh() < cur.getHigh()) {
                high.add(cur);
                //添加支撑线，为前一个的相对低点
                if (!low.isEmpty()) {
                    sList.add(low.get(low.size() - 1).getLow());
                }
            }

//             最值点
            if (priceList.get(maxIdx).getHigh() < next.getHigh()) {
                maxIdx = priceList.size() - 1;
            }
            if (priceList.get(minIdx).getLow() > next.getLow()) {
                minIdx = priceList.size() - 1;
            }
            pre = cur;
            cur = next;
        }
        return new int[]{maxIdx, minIdx};
    }
}



