package com.helei.telegramebot.bot.impl;

import com.helei.dto.base.Result;
import com.helei.solanarpc.dto.SolanaAddress;
import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.SolanaTelegramBot;
import com.helei.telegramebot.bot.menu.solana.SolanaBotMenu;

import com.helei.telegramebot.config.command.SolanaBotCommand;
import com.helei.telegramebot.config.command.TelegramBotNameSpaceCommand;
import com.helei.telegramebot.service.ISolanaATBotPersistenceService;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.concurrent.ExecutorService;


@Setter
@Slf4j
public class SolanaAutoTradeTelegramBot extends MenuBaseTelegramBot implements SolanaTelegramBot {


    private ISolanaATBotPersistenceService solanaATBotPersistenceService;


    public SolanaAutoTradeTelegramBot(
            String botUsername,
            String token,
            ITelegramPersistenceService telegramPersistenceService,
            ExecutorService executor
    ) {
        super(botUsername, token, telegramPersistenceService, executor);
        super.init(new SolanaBotMenu(getBotUsername(), getTelegramPersistenceService()));
    }


    @Override
    public Result commandMessageHandler(TelegramBotNameSpaceCommand.NameSpace nameSpace, String nameSpaceCommand, List<?> params, Message message) {
        SolanaBotCommand solanaBotCommand = SolanaBotCommand.valueOf(nameSpaceCommand);

        return switch (solanaBotCommand) {
            case BIND_WALLET_ADDRESS -> bindWalletAddress(params, message);
            case ADD_LISTEN_ACCOUNT -> updateTransactionListenAccount(params, message);
            case CANCEL_LISTEN_ACCOUNT -> cancelTransactionListenAccount(params, message);
        };
    }


    @Override
    public Result normalMessageHandler(String messageText, Message message) {
        return null;
    }

    @Override
    public Result bindWalletAddress(List<?> params, Message message) {
        //Step 1 参数校验
        if (params.isEmpty()) return Result.fail("参数错误");

        //Step 2 解析参数
        String address = (String) params.getFirst();

        return solanaATBotPersistenceService.bindWalletAddress(getBotUsername(), String.valueOf(message.getChatId()), address);
    }


    @Override
    public Result updateTransactionListenAccount(List<?> params, Message message) {
        //Step 1 参数校验
        if (params.isEmpty()) return Result.fail("参数错误");

        //Step 2 解析参数
        String address = (String) params.getFirst();
        String name = "";
        if (params.size() >= 2) {
            name = (String) params.get(1);
        }
        String description = "";
        if (params.size() >= 3) {
            description = (String) params.get(2);
        }

        SolanaAddress solanaAddress = SolanaAddress
                .builder()
                .accountAddress(address)
                .name(name)
                .description(description)
                .build();

        //Step 3 保存
        return solanaATBotPersistenceService.updateChatListenAddress(getBotUsername(), String.valueOf(message.getChatId()), solanaAddress);
    }

    @Override
    public Result cancelTransactionListenAccount(List<?> params, Message message) {
        //Step 1 参数校验
        if (params.isEmpty()) return Result.fail("参数错误");

        //Step 2 解析参数
        String address = (String) params.getFirst();

        //Step 3 删除
        return solanaATBotPersistenceService.deleteChatListenAddress(getBotUsername(), String.valueOf(message.getChatId()), address);
    }
}

