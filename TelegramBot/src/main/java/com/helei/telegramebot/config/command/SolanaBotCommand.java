package com.helei.telegramebot.config.command;

public enum SolanaBotCommand {


    /**
     * 绑定钱包地址 /SolanaBotCommand.bind_wallet_address address  绑定钱包地址
     */
    BIND_WALLET_ADDRESS,

    /**
     * 添加监听的账户, /SolanaBotCommand.add_listen_account address name
     */
    ADD_LISTEN_ACCOUNT,

    /**
     * 取消监听账户 /SolanaBotCommand.CANCEL_LISTEN_ACCOUNT address
     */
    CANCEL_LISTEN_ACCOUNT
}

