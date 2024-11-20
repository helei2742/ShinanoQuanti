package com.helei.telegramebot.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.helei.dto.trade.TradeSignal;
import com.helei.telegramebot.bot.AbstractTelegramBot;
import com.helei.telegramebot.config.command.TelegramBotNameSpaceCommand;
import com.helei.telegramebot.config.command.TradeSignalCommand;
import com.helei.telegramebot.manager.ExecutorServiceManager;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * kafka交易信号监听， 会将其发到telegram bot，由其发送消息到群组
 */
@Slf4j
public class KafkaTradeSignalTGBotListener implements MessageListener<String, String> {

    private final AbstractTelegramBot telegramBot;

    private final ExecutorService executor;

    public KafkaTradeSignalTGBotListener(AbstractTelegramBot telegramBot, ExecutorServiceManager executorServiceManager) {
        this.telegramBot = telegramBot;
        this.executor = executorServiceManager.getCommonExecutor();
    }


    @Override
    public void onMessage(@NotNull ConsumerRecord<String, String> record) {
        executor.execute(()->{
            String topic = record.topic();
            String value = record.value();
            log.info("topic[{}]收到消息[{}]", topic, value);

            if (StrUtil.isBlank(value)) {
                log.warn("receive null kafka trade signal, topic[{}] key [{}]", topic, record.key());
                return;
            }

            try {
                TradeSignal tradeSignal = JSONObject.parseObject(value, TradeSignal.class);

                telegramBot.commandMessageHandler(
                        TelegramBotNameSpaceCommand.NameSpace.TRADE_SIGNAL_COMMAND,
                        TradeSignalCommand.SEND_TRADE_SIGNAL.name(),
                        List.of(tradeSignal),
                        null
                );
            } catch (Exception e) {
                log.error("处理kafka topic[{}] 消息[{}]时出错", topic, value, e);
            }
        });
    }
}
