package com.helei.util;

import com.helei.constants.CEXType;
import com.helei.constants.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;

public class KafkaUtil {

    public static final String TOPIC_KLINE_FORMAT = "topic.%s.%s.%s.%s";

    /**
     * 获取kafka里topic的名字
     *
     * @param cexType    cexType
     * @param streamName streamName
     * @param runEnv runEnv
     * @param type       type 合约或现货
     * @return topic
     */
    public static String resolveKafkaTopic(CEXType cexType, String streamName, RunEnv runEnv, TradeType type) {
        streamName = streamName.replace("@", "_");
        return String.format(TOPIC_KLINE_FORMAT, cexType.getDescription(), streamName, runEnv.name().toLowerCase(), type.getDescription()).toLowerCase();
    }


    public static String getKLineStreamName(String symbol, KLineInterval interval) {
        return symbol + "_kline_" + interval.getDescribe();
    }
}
