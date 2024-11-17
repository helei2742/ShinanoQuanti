package com.helei.telegramebot.template;

import com.helei.dto.trade.TradeSignal;

import java.time.Instant;

/**
 * TG 消息模板
 */
public class TelegramMessageTemplate {
    private static final String TRADE_SIGNAL_MESSAGE_TEMPLATE
            = """
            
            <b>%s</b>
            方向:<b>%s</b>
            入场价:<b>%s</b>
            止盈:<b>%s</b>
            止损:<b>%s</b>
            ----------------------
            信号名:<b>%s</b>
            K线时间:<b>%s</b>
            信号时间:<b>%s</b>
            
            """;


    /**
     * 交易信号消息
     *
     * @param tradeSignal 信号
     * @return 结构化后的消息
     */
    public static String tradeSignalMessage(TradeSignal tradeSignal) {
        return String.format(TRADE_SIGNAL_MESSAGE_TEMPLATE,
                tradeSignal.getSymbol(),
                tradeSignal.getTradeSide().name(),
                tradeSignal.getEnterPrice(),
                tradeSignal.getTargetPrice(),
                tradeSignal.getStopPrice(),
                tradeSignal.getName(),
                Instant.ofEpochMilli(tradeSignal.getCreateKLineOpenTimestamp()),
                Instant.ofEpochMilli(tradeSignal.getCreateKLineOpenTimestamp())
        );
    }
}
