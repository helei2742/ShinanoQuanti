package com.helei.tradesignalprocess.stream.a_datasource;


import com.helei.constants.KLineInterval;

import java.util.List;

@Deprecated
public interface KLineDataPublisher {

    KLineDataPublisher addListenKLine(String symbol, List<KLineInterval> intervalList);
}

