package com.helei.tradesignalprocess.stream.b_indicator.calculater;

import com.helei.dto.trade.KLine;
import com.helei.dto.indicator.MACD;
import com.helei.dto.indicator.config.MACDConfig;
import com.helei.util.CalculatorUtil;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;

public class MACDCalculator extends BaseIndicatorCalculator<MACD> {
    private final int ema1Period;

    private final int ema2Period;

    private final int deaPeriod;

    private transient ValueState<MACD> macdState;


    public MACDCalculator(MACDConfig macdConfig) {
        super(macdConfig);
        this.ema1Period = macdConfig.getEma1Period();
        this.ema2Period = macdConfig.getEma2Period();
        this.deaPeriod = macdConfig.getDeaPeriod();
    }


    @Override
    public void open(Configuration parameters, RuntimeContext runtimeContext) throws Exception {
        ValueStateDescriptor<MACD> descriptor = new ValueStateDescriptor<>("macdState", TypeInformation.of(MACD.class));
        macdState = runtimeContext.getState(descriptor);
    }

//    @Override
//    public String indicatorKey(MACD indicator) {
//        return "MACE" +  ema1Period + "-"  + ema2Period + "-" + deaPeriod;
//    }

    @Override
    public MACD calculateInKLine(KLine kLine) throws IOException {
        Double close = kLine.getClose();
        MACD macd = macdState.value();

        if (macd == null) {
            macd = new MACD(close, close, close);
        }

        double ema1 = macd.getEma1();
        double ema2 = macd.getEma2();
        double dea = macd.getDea();

        // 计算新的 EMA12 和 EMA26

        ema1 = CalculatorUtil.calculateEMA(close, ema1, ema1Period);
        ema2 = CalculatorUtil.calculateEMA(close, ema2, ema2Period);

        // 计算 DIF
        double dif = ema1 - ema2;
        dea = CalculatorUtil.calculateEMA(dif, dea, deaPeriod);

        macd.setEma1(ema1);
        macd.setEma2(ema2);
        macd.setDea(dea);
        macdState.update(macd);
        return macd;
    }

}

