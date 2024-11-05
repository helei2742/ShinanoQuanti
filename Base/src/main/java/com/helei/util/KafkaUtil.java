package com.helei.util;

import com.helei.constants.CEXType;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;

public class KafkaUtil {

    public static final String TOPIC_KLINE_FORMAT = "topic.%s.%s.%s.%s";

    private static final String TOPIC_ORDER_FORMAT = "topic.%s.%s.order.%s";

    /**
     * 获取kafka里topic的名字
     *
     * @param cexType    cexType
     * @param streamName streamName
     * @param runEnv     runEnv
     * @param type       type 合约或现货
     * @return topic
     */
    public static String resolveKafkaTopic(CEXType cexType, String streamName, RunEnv runEnv, TradeType type) {
        streamName = streamName.replace("@", "_");
        return String.format(TOPIC_KLINE_FORMAT, cexType.getDescription(), streamName, runEnv.name().toLowerCase(), type.getDescription()).toLowerCase();
    }

    /**
     * 获取写入订单的topic
     *
     * @param runEnv runEnv
     * @param type   tradeType
     * @param symbol 交易对
     * @return topic
     */
    public static String getOrderSymbolTopic(RunEnv runEnv, TradeType type, String symbol) {
        return String.format(TOPIC_ORDER_FORMAT, runEnv.name(), type.name(), symbol);
    }


    public static String getKLineStreamName(String symbol, KLineInterval interval) {
        return symbol + "_kline_" + interval.getDescribe();
    }
}

