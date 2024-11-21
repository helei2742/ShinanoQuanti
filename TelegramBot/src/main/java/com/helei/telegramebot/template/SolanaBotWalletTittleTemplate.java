package com.helei.telegramebot.template;

import com.helei.telegramebot.entity.ChatWallet;

public class SolanaBotWalletTittleTemplate {
    private static final String A_CHAT_WALLET_TEMPLATE = """
                钱包 %d: %s
                地址: %s
                钱包余额: %f SOL
            """;


    public static String aChatWalletPrintStr(ChatWallet chatWallet, Long idx) {
        return String.format(A_CHAT_WALLET_TEMPLATE, idx, chatWallet.getName(), chatWallet.getPublicKey(), chatWallet.getSolAmount());
    }

}
