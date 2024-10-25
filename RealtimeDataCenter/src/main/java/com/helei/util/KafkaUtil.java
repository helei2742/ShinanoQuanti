package com.helei.util;

import com.helei.constants.KLineInterval;
import com.helei.constants.TradeType;

public class KafkaUtil {

    public static final String TOPIC_KLINE_FORMAT = "topic.%s.%s.%s";

    /**
     * 获取kafka里topic的名字
     * @param cexName cexName
     * @param streamName streamName
     * @param type type 合约或现货
     * @return topic
     */
    public static String resolveKafkaTopic(String cexName, String streamName, TradeType type) {
        streamName = streamName.replace("@", "_").toLowerCase();
        return String.format(TOPIC_KLINE_FORMAT, cexName, streamName, type.getDescription());
    }


    public static String getKLineStreamName(String symbol, KLineInterval interval) {
        return symbol + "_kline_" + interval.getDescribe();
    }
}
