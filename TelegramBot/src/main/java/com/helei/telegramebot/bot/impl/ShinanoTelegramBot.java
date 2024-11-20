package com.helei.telegramebot.bot.impl;

import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.TradeType;
import com.helei.dto.base.Result;
import com.helei.dto.trade.TradeSignal;
import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.TradeSignalCommandTelegramBot;
import com.helei.telegramebot.config.command.TelegramBotNameSpaceCommand;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import com.helei.telegramebot.service.ITradeSignalPersistenceService;
import com.helei.telegramebot.service.impl.KafkaConsumerService;
import com.helei.telegramebot.template.TelegramMessageTemplate;
import com.helei.util.KafkaUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class ShinanoTelegramBot extends MenuBaseTelegramBot implements TradeSignalCommandTelegramBot {


    private final KafkaConsumerService kafkaConsumerService;

    @Setter
    private ITradeSignalPersistenceService tradeSignalPersistenceService;

    public ShinanoTelegramBot(
            String botUsername,
            String token,
            ITelegramPersistenceService telegramPersistenceService,
            KafkaConsumerService kafkaConsumerService,
            ExecutorService executorService
    ) {
        super(botUsername, token, telegramPersistenceService, executorService);
        this.kafkaConsumerService = kafkaConsumerService;
    }


    @Override
    public boolean commandMessageFilter(TelegramBotNameSpaceCommand.NameSpace nameSpace, String nameSpaceCommand, List<String> params, Message message) {
        Result result = getTelegramPersistenceService().isSavedChatInBot(getBotUsername(), message.getChatId());

        if (!result.getSuccess()) {
            sendMessageToChat(String.valueOf(message.getChatId()), result.getErrorMsg());
            return true;
        }

        return false;
    }

    @Override
    public Result commandMessageHandler(TelegramBotNameSpaceCommand.NameSpace nameSpace, String nameSpaceCommand, List<?> params, Message message) {
        return switch (nameSpaceCommand) {
            case "ADD_LISTEN_SIGNAL_TYPE" -> addListenSignalTypeCommandHandler(params, message);
            case "SEND_TRADE_SIGNAL" -> sendTradeSignalCommandHandler(params);
            default -> Result.fail(String.format("namespace[%s]没有命令[%S]", nameSpace, nameSpaceCommand));
        };
    }

    @Override
    public Result normalMessageHandler(String messageText, Message message) {
        return null;
    }

    /**
     * 处理用户发送的开始命令
     *
     * @param message 原消息体
     */
    @Override
    public void startCommandHandler(Message message) {
        Long chatId = message.getChatId();

        // chatId持久化，连同用户信息
        User from = message.getFrom();

        Result result = getTelegramPersistenceService().saveChatInBot(getBotUsername(), chatId, from);
        if (!result.getSuccess()) {
            log.error("保存聊天[{}]用户[{}]信息失败", chatId, from.getUserName());
            sendMessageToChat(String.valueOf(chatId), result.getErrorMsg());
        } else {
            sendMessageToChat(String.valueOf(chatId), getBotUsername() + " 注册聊天信息成功");
        }
    }

    /**
     * 处理发送信号命令
     *
     * @param params 参数
     */
    @Override
    public Result sendTradeSignalCommandHandler(List<?> params) {
        //Step 1 参数解析
        TradeSignal tradeSignal = (TradeSignal) params.getFirst();
        String message = TelegramMessageTemplate.tradeSignalMessage(tradeSignal);


        //Step 2 查询监听的id
        Result result = tradeSignalPersistenceService
                .queryTradeSignalListenedChatId(getBotUsername(), tradeSignal.getRunEnv(), tradeSignal.getTradeType(), tradeSignal.getCexType(), tradeSignal.getSymbol());


        //Step 3 发送
        if (result.getSuccess()) {
            // 获取chatId成功
            Collection<?> chatIds = (Collection<?>) result.getData();

            if (chatIds.isEmpty()) {
                log.warn("没有监听信号[{}]的chat", tradeSignal.simpleName());
                return result;
            }

            for (Object chatId : chatIds) {
                CompletableFuture
                        .runAsync(() -> {
                            sendHTMLMessageToChat((String) chatId, message);
                        }, executor)
                        .exceptionallyAsync(throwable -> {
                            log.error("向chatId[{}]发送信号[{}][{}]信息发生错误", chatId, tradeSignal.simpleName(), tradeSignal.getId());
                            return null;
                        }, executor);
            }

            return result;
        } else {
            // 获取chatId失败
            return Result.fail("获取chatId失败, " + result.getErrorMsg());
        }
    }


    /**
     * 添加监听信号命令
     *
     * @param params  参数 [runEnv, tradeType, cexType, 可选symbols]
     * @param message 消息
     */
    @Override
    public Result addListenSignalTypeCommandHandler(List<?> params, Message message) {
        String chatId = String.valueOf(message.getChatId());

        if (params.size() <= 3) {
            return Result.fail(String.format("参数错误,命令[%s]参数格式应为[runEnv, tradeType, cexType, symbols(可选)]", "ADD_LISTEN_SIGNAL_TYPE"));
        }

        log.info("机器人[{}]-group chat id[{}] 添加监听信号[{}]", getBotUsername(), chatId, params);

        //Step 1 解析初始化参数
        RunEnv runEnv = RunEnv.valueOf(((String) params.getFirst()).toUpperCase());
        TradeType tradeType = TradeType.valueOf(((String) params.get(1)).toUpperCase());
        CEXType cexType = CEXType.valueOf(((String) params.get(2)).toUpperCase());

        String symbol = (String) params.get(3);
        String signalName = (String) params.get(4);


        //Step 2 持久化
        Result result = tradeSignalPersistenceService
                .saveChatListenTradeSignal(getBotUsername(), chatId, runEnv, tradeType, cexType, symbol, signalName);

        //Step 3 注册kafka消费者
        String topic = KafkaUtil.getTradeSingalTopic(runEnv, tradeType, symbol, signalName);
        kafkaConsumerService.startTelegramBotTradeSignalConsumer(List.of(topic));

        if (result.getSuccess()) {
            result.setData(String.format("添加信号[%s]-[%s]-[%s]-[%s]-%s成功", runEnv.name(), tradeType.name(), cexType.name(), symbol, signalName));
        }

        return result;
    }

}


