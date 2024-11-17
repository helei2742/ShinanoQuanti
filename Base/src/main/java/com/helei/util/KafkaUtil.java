package com.helei.util;

import com.helei.constants.CEXType;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;

public class KafkaUtil {

    public static final String TOPIC_KLINE_FORMAT = "market.%s.%s.%s.%s";

    private static final String TOPIC_ORDER_FORMAT = "order.%s.%s.order.%s";

    private static final String TOPIC_SIGNAL_FORMAT = "signal.%s.%s.%s.%s";

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

    /**
     * 获取信号的topic
     *
     * @param runEnv     运行环境
     * @param type       交易类型
     * @param symbol     交易对
     * @param signalName 信号名
     * @return topic
     */
    public static String getTradeSingalTopic(RunEnv runEnv, TradeType type, String symbol, String signalName) {
        return String.format(TOPIC_SIGNAL_FORMAT, runEnv.name(), type.name(), symbol, signalName).toLowerCase();
    }


    public static String getKLineStreamName(String symbol, KLineInterval interval) {
        return symbol + "_kline_" + interval.getDescribe();
    }
}
