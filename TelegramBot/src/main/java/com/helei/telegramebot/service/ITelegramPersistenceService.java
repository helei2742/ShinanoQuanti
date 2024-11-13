package com.helei.telegramebot.service;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.Result;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

public interface ITelegramPersistenceService {


    /**
     * 保存聊天用户信息
     *
     * @param chatId 聊天id
     * @param user   用户
     * @return 是否保存成功
     */
    Result saveChatUser(Long chatId, User user);

    /**
     * 保存群组信息
     *
     * @param chat 群组信息
     * @return 是否成功
     */
    Result saveGroupChat(Chat chat);

    /**
     * 保存chat监听的交易信号
     *
     * @param chatId    chatId
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @param cexType   cexType
     * @param symbols   symbols
     * @return 是否成功
     */
    Result saveChatListenTradeSignal(String chatId, RunEnv runEnv, TradeType tradeType, CEXType cexType, List<String> symbols);


    /**
     * 查询监听交易对信号的tg chatId
     *
     * @param runEnv    runEnv
     * @param tradeType tradeType
     * @param cexType   cexType
     * @param symbol    symbol
     * @return chatId list
     */
    Result queryTradeSignalListenedChatId(RunEnv runEnv, TradeType tradeType, CEXType cexType, String symbol);
}
