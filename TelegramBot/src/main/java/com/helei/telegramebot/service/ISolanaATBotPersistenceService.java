package com.helei.telegramebot.service;

import com.helei.dto.base.Result;
import com.helei.solanarpc.dto.SolanaAddress;
import com.helei.telegramebot.entity.ChatWallet;

import java.util.List;

public interface ISolanaATBotPersistenceService {


    /**
     * 绑定钱包地址
     *
     * @param chatId     chatId
     * @param privateKey privateKey
     * @return Result
     */
    Result bindWalletByPrivateKey(String botUsername, String chatId, String privateKey);


    /**
     * 更新默认钱包地址
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @param pubKey      pubKey
     */
    void updateDefaultWalletAddress(String botUsername, String chatId, String pubKey, boolean isCover);

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
     * @param chatId  chatId
     * @param address 钱包地址
     * @return Result
     */
    Result deleteChatListenAddress(String botUsername, String chatId, String address);


    /**
     * 查默认钱包地址
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return publicKey （address）
     */
    String queryChatIdDefaultWalletAddress(String botUsername, String chatId);


    /**
     * 查询chatId绑定的所有钱包
     *
     * @param botUsername botUsername
     * @param chatId      chatId
     * @return List<ChatWallet>
     */
    List<ChatWallet> queryChatIdAllWallet(String botUsername, String chatId);


    /**
     * 查询钱包信息
     *
     * @param query query
     * @return ChatWallet
     */
    ChatWallet queryChatIdWallet(ChatWallet query);


    /**
     * 保存chat 钱包
     *
     * @param chatWallet chatWallet
     * @return 是否成功
     */
    boolean saveChatWallet(ChatWallet chatWallet);
}
