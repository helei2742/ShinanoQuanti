package com.helei.telegramebot.config.command;



/**
 * tg 机器人命令
 * start - 启动
 * TRADE_SIGNAL_COMMAND.add_listen_signal_type - 添加监听的信号类型，参数[runEnv, tradeType, cexType, symbol, signalName]
 *
 */
public enum TelegramBotCommand {

    /**
     * 用户第一次点bot时发送的start命令
     */
    START,
    ;

}
