package com.helei.telegramebot.bot;


import com.helei.dto.base.Result;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface TradeSignalCommandTelegramBot {


    /**
     * 添加监听信号命令
     *
     * @param params  参数 [runEnv, tradeType, cexType, 可选symbols]
     * @param message 消息
     */
    Result addListenSignalTypeCommandHandler(List<?> params, Message message);

    /**
     * 处理发送信号命令
     *
     * @param params 参数
     */
    Result sendTradeSignalCommandHandler(List<?> params);
}


