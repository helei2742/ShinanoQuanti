package com.helei.tradesignalcenter.resolvestream.a_datasource;


import com.helei.constants.KLineInterval;

import java.util.List;

public interface KLineDataPublisher {

    KLineDataPublisher addListenKLine(String symbol, List<KLineInterval> intervalList);
}

