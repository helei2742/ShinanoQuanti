package com.helei.tradedatacenter.indicator.calculater;

import cn.hutool.core.util.BooleanUtil;
import com.helei.tradedatacenter.dto.TrendLine;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.PST;
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

    public PSTCalculator(
            String name,
            int windowLength,
            int pCount,
            int sCount
    ) {
        super(name);
        this.windowLength = windowLength;
        this.pCount = pCount;
        this.sCount = sCount;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.windowDataState =  getRuntimeContext().getListState(new ListStateDescriptor<>("windowDataState", TypeInformation.of(KLine.class)));
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

        int maxIdxInHigh = high.indexOf(priceList.get(maxIdx));
        int minIdxInLow = low.indexOf(priceList.get(minIdx));

        TrendLine upTrend = null;
        if (maxIdxInHigh == 0 || maxIdxInHigh == high.size() - 1 || maxIdxInHigh == -1) {
            //最高点是相对高点的第一个或，最后一个，说明相对高点依次变高或依次变低，整个窗口趋势一致
            //趋势线取全部
            upTrend = TrendLine.calculateTrend(high, KLine::getHigh);
        } else  {
            //最高点在相对高点中间，取最高点到末尾
            List<KLine> kLines = high.subList(maxIdxInHigh, high.size() - 1);
            upTrend = TrendLine.calculateTrend(kLines, KLine::getHigh);
        }

        TrendLine downTrend = null;
        if (minIdxInLow == 0 || minIdxInLow == low.size() - 1 || minIdxInLow == -1) {
            //最低点在相对低点的首尾，或者不在相对低点里，单一趋势
            //趋势线取全部
            downTrend = TrendLine.calculateTrend(low, KLine::getLow);
        } else {
            downTrend = TrendLine.calculateTrend(low.subList(minIdxInLow, low.size() - 1), KLine::getLow);
        }

        return PST
                .builder()
                .pressure(new ArrayList<>(pList.subList(0, Math.max(0,  Math.min(pList.size()-1, pCount)))))
                .support(new ArrayList<>(sList.subList(0, Math.max(0, Math.min(sList.size()-1, sCount)))))
                .relativeUpTrendLine(upTrend)
                .relativeDownTrendLine(downTrend)
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
