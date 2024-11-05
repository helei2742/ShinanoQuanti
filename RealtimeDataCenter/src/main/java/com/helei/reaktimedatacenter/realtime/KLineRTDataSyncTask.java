package com.helei.reaktimedatacenter.realtime;

import cn.hutool.core.lang.Pair;
import com.helei.constants.trade.KLineInterval;

import java.util.List;

public abstract class KLineRTDataSyncTask {

    protected final List<Pair<String, KLineInterval>> listenKLines;

    protected KLineRTDataSyncTask(List<Pair<String, KLineInterval>> listenKLines) {
        this.listenKLines = listenKLines;
    }
}
