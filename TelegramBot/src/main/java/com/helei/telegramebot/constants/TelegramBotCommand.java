package com.helei.telegramebot.constants;


/**
 * tg 机器人命令+
 */
public enum TelegramBotCommand {

    /**
     * 用户第一次点bot时发送的start命令
     */
    START,

    /**
     * 机器人加入群组后，发送此命令，将该群组id记录
     */
    ADD_LISTEN_SIGNAL_TYPE,

    /**
     * 发送交易信号
     */
    SEND_TRADE_SIGNAL
}
