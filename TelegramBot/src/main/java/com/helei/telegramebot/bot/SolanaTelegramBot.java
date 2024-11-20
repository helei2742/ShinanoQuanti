package com.helei.telegramebot.bot;

import com.helei.dto.base.Result;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface SolanaTelegramBot {


    /**
     * 绑定钱包地址
     *
     * @param params  参数
     * @param message message
     * @return Result
     */
    Result bindWalletAddress(List<?> params, Message message);

    /**
     * 更新监听交易的账户
     *
     * @param params  参数
     * @param message message
     * @return Result
     */
    Result updateTransactionListenAccount(List<?> params, Message message);


    /**
     * 取消监听账户地址交易
     *
     * @param params  参数
     * @param message message
     * @return Result
     */
    Result cancelTransactionListenAccount(List<?> params, Message message);
}


