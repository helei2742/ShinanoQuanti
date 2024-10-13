package com.helei.tradedatacenter.datasource;

import com.helei.cexapi.binanceapi.constants.KLineInterval;

import java.time.LocalDateTime;
import java.util.List;

public interface KLineDataPublisher {

    KLineDataPublisher addListenKLine(String symbol, List<KLineInterval> intervalList);
}

