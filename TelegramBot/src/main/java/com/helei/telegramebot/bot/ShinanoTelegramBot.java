package com.helei.telegramebot.bot;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.Result;
import com.helei.dto.trade.TradeSignal;
import com.helei.telegramebot.constants.TelegramBotCommand;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import com.helei.telegramebot.template.TelegramMessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class ShinanoTelegramBot extends AbstractTelegramBot {

    public ShinanoTelegramBot(
            String botUsername,
            String token,
            ITelegramPersistenceService telegramPersistenceService,
            ExecutorService executorService
    ) {
        super(botUsername, token, telegramPersistenceService, executorService);
    }


    @Override
    public void normalMessageHandler(String messageText, Message message) {

    }

    @Override
    public void commandMessageHandler(TelegramBotCommand command, List<?> params, Message message) {
        switch (command) {
            case START -> startCommandHandler(message);
            case ADD_LISTEN_SIGNAL_TYPE -> addListenSignalTypeCommandHandler(params, message);
            case SEND_TRADE_SIGNAL -> sendTradeSignalCommandHandler(params);
        }
    }


    /**
     * 添加监听信号命令
     *
     * @param params  参数 [runEnv, tradeType, cexType, 可选symbols]
     * @param message 消息
     */
    private void addListenSignalTypeCommandHandler(List<?> params, Message message) {
        String chatId = String.valueOf(message.getChatId());

        if (params.size() <= 3) {
            sendMessageToChat(chatId, String.format("参数错误,命令[%s]参数格式应为[runEnv, tradeType, cexType, symbols(可选)]", TelegramBotCommand.ADD_LISTEN_SIGNAL_TYPE.name()));
            return;
        }

        log.info("开始初始化群机器人[{}]-group chat id[{}]", getBotUsername(), chatId);

        //Step 1 解析初始化参数
        RunEnv runEnv = RunEnv.valueOf((String) params.getFirst());
        TradeType tradeType = TradeType.valueOf((String) params.get(1));
        CEXType cexType = CEXType.valueOf((String) params.get(2));
        List<String> symbols = new ArrayList<>();
        for (int i = 3; i < params.size(); i++) {
            symbols.add((String) params.get(i));
        }

        //Step 2 持久化
        Result result = getTelegramPersistenceService().saveChatListenTradeSignal(chatId, runEnv, tradeType, cexType, symbols);
        if (!result.getSuccess()) {
            sendMessageToChat(chatId, result.getErrorMsg());
        }
    }

    /**
     * 处理用户发送的开始命令
     *
     * @param message 原消息体
     */
    private void startCommandHandler(Message message) {
        Long chatId = message.getChatId();

        // chatId持久化，连同用户信息
        User from = message.getFrom();

        Result result = getTelegramPersistenceService().saveChatUser(chatId, from);
        if (!result.getSuccess()) {
            log.error("保存聊天[{}]用户[{}]信息失败", chatId, from.getUserName());
            sendMessageToChat(String.valueOf(chatId), result.getErrorMsg());
        }
    }

    /**
     * 处理发送信号命令
     *
     * @param params 参数
     */
    private void sendTradeSignalCommandHandler(List<?> params) {
        //Step 1 参数解析
        TradeSignal tradeSignal = (TradeSignal) params.getFirst();
        String message = TelegramMessageTemplate.tradeSignalMessage(tradeSignal);


        //Step 2 查询监听的id
        Result result = getTelegramPersistenceService()
                .queryTradeSignalListenedChatId(tradeSignal.getRunEnv(), tradeSignal.getTradeType(), tradeSignal.getCexType(), tradeSignal.getSymbol());


        //Step 3 发送
        if (result.getSuccess()) {
            // 获取chatId成功
            Collection<?> chatIds = (Collection<?>) result.getData();
            for (Object chatId : chatIds) {
                CompletableFuture
                        .runAsync(() -> {
                            sendMessageToChat((String) chatId, message);
                        }, executor)
                        .exceptionallyAsync(throwable -> {
                            log.error("向chatId[{}]发送信号[{}]信息发生错误", chatId, tradeSignal.getId());
                            return null;
                        }, executor);
            }
        } else {
            // 获取chatId失败
            log.error("获取chatId失败, [{}]", result.getErrorMsg());
        }
    }
}
