package com.helei.tradedatacenter.datasource;

import com.helei.tradedatacenter.constants.KLineInterval;

import java.util.List;

public interface KLineDataPublisher {

    KLineDataPublisher addListenKLine(String symbol, List<KLineInterval> intervalList);

}

