package com.helei.telegramebot.service;

import com.helei.dto.base.Result;
import com.helei.solanarpc.dto.SolanaAddress;

public interface ISolanaATBotPersistenceService {


    /**
     * 绑定钱包地址
     *
     * @param chatId  chatId
     * @param address address
     * @return Result
     */
    Result bindWalletAddress(String botUsername, String chatId, String address);


    /**
     * 保存chatId跟踪的钱包地址
     *
     * @param chatId        chatId
     * @param solanaAddress 钱包地址
     * @return Result
     */
    Result updateChatListenAddress(String botUsername, String chatId, SolanaAddress solanaAddress);


    /**
     * 删除chatId跟踪的钱包地址
     *
     * @param chatId        chatId
     * @param address 钱包地址
     * @return Result
     */
    Result deleteChatListenAddress(String botUsername, String chatId, String address);

}

