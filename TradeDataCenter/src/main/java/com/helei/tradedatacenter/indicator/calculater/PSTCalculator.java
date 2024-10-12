package com.helei.tradedatacenter.indicator.calculater;

import com.helei.tradedatacenter.dto.TrendLine;
import com.helei.tradedatacenter.entity.KLine;
import com.helei.tradedatacenter.indicator.PST;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
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

    protected PSTCalculator(
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
        this.windowDataState =  getRuntimeContext().getListState(new ListStateDescriptor<>("windowDataState", KLine.class));
    }

    @Override
    public PST calculateInKLine(KLine kLine) throws Exception {
        LinkedList<KLine> priceList = new LinkedList<>();
        Iterator<KLine> iterator = windowDataState.get().iterator();

        if (!iterator.hasNext()) {
            return null;
        }
        KLine pre = iterator.next();
        if (!iterator.hasNext()) {
            return null;
        }

        KLine cur = iterator.next();

        priceList.add(pre);
        priceList.add(cur);

        // 相对高点
        List<KLine> high = new ArrayList<>();
        // 相对低点
        List<KLine> low = new ArrayList<>();
        //压力
        List<Double> pList = new ArrayList<>();
        //支持
        List<Double> sList = new ArrayList<>();

        int maxIdx = 0;
        int minIdx = 0;

        //取趋势线,
        boolean addCur = false;
        while (iterator.hasNext() || !addCur) {
            KLine next = null;
            if (iterator.hasNext()) {
                next = iterator.next();
            } else {
                next = kLine;
                addCur = true;
            }

            priceList.add(next);

            //相对低点
            if (cur.getClose() < next.getClose() && pre.getClose() > cur.getClose()) {
                low.add(cur);
                //添加压力线，为前一个的相对高点
                if (!high.isEmpty()) {
                    pList.add(high.get(high.size() - 1).getHigh());
                }
            }

            //相对高点
            if (cur.getClose() > next.getClose() && pre.getClose() < cur.getClose()) {
                high.add(cur);
                //添加支撑线，为前一个的相对低点
                if (!low.isEmpty()) {
                    sList.add(low.get(low.size() - 1).getLow());
                }
            }

            //最值点
            if (priceList.get(maxIdx).getHigh() < next.getHigh()) {
                maxIdx = priceList.size() - 1;
            }
            if (priceList.get(minIdx).getLow() > next.getLow()) {
                minIdx = priceList.size() - 1;
            }
        }

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


        if (priceList.size() > windowLength) {
            priceList.removeFirst();
        }
        windowDataState.update(priceList);

        return PST
                .builder()
                .pressure(pList.subList(Math.max(0, pList.size() - pCount), pList.size() - 1))
                .support(sList.subList(Math.max(0, sList.size() - sCount), sList.size() - 1))
                .relativeUpTrendLine(upTrend)
                .relativeDownTrendLine(downTrend)
                .maxPrice(priceList.get(maxIdx).getHigh())
                .minPrice(priceList.get(minIdx).getLow())
                .build();
    }
}