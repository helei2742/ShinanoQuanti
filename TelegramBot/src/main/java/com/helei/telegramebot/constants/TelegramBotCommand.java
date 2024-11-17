package com.helei.telegramebot.constants;


/**
 * tg 机器人命令
 * start - 启动
 * add_listen_signal_type - 添加监听的信号类型，参数[runEnv, tradeType, cexType, symbol, signalName]
 *
 */
public enum TelegramBotCommand {

    /**
     * 用户第一次点bot时发送的start命令
     */
    START,

    /**
     * 添加监听的信号类型
     */
    ADD_LISTEN_SIGNAL_TYPE,

    /**
     * 发送交易信号
     */
    SEND_TRADE_SIGNAL
}
