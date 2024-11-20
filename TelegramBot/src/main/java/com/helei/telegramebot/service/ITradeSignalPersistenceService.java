package com.helei.telegramebot.service;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.Result;

public interface ITradeSignalPersistenceService {
    /**
     * 保存chat监听的交易信号
     *
     * @param botUsername 机器人用户名
     * @param chatId      chatId
     * @param runEnv      runEnv
     * @param tradeType   tradeType
     * @param cexType     cexType
     * @param symbol     symbol
     * @param signalName     signalName
     * @return 是否成功
     */
    Result saveChatListenTradeSignal(String botUsername, String chatId, RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol, String signalName);


    /**
     * 查询监听交易对信号的tg chatId
     *
     * @param botUsername 机器人用户名
     * @param runEnv      runEnv
     * @param tradeType   tradeType
     * @param cexType     cexType
     * @param symbol      symbol
     * @return chatId list
     */
    Result queryTradeSignalListenedChatId(String botUsername, RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol);
}
