package com.helei.telegramebot.bot.menu.solana.wallet;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.helei.dto.base.Result;
import com.helei.solanarpc.util.SolanaKeyAddressUtil;
import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import com.helei.telegramebot.entity.ChatWallet;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class BindWalletMenuNode extends AbstractWalletMenuNode {


    public BindWalletMenuNode(TGMenuNode parentMenu) {
        super(parentMenu, "绑定钱包", "BIND_WALLET");
    }

    @Override
    public SendMessage buildDynamicMenu(String chatId) {
        SendMessage dynamicMenu = new SendMessage();
        dynamicMenu.setChatId(chatId);
        dynamicMenu.setText("请输入需要绑定的钱包私匙");

        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);

        dynamicMenu.setReplyMarkup(forceReplyKeyboard);

        return dynamicMenu;
    }

    @Override
    protected BotApiMethod<?> menuCommandHandler(MenuBaseTelegramBot bot, List<String> params, Message message) {
        String chatId = String.valueOf(message.getChatId());

        //Step 1 参数校验
        String privateKey = params.getFirst();
        if (StrUtil.isBlank(privateKey)) {
            log.error("chatId[{}] - [{}]命令参数为空", getButtonText(), chatId);
            return null;
        }

        //Step 2 保存
        Result result = persistenceService.bindWalletByPrivateKey(
                bot.getBotUsername(),
                chatId,
                privateKey
        );

        //Step 4 返回结果
        // 编辑消息内容为空或替换为提示文字
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (!result.getSuccess()) {
            sendMessage.setText("保存钱包信息失败, " + result.getErrorMsg()); // 替换原内容
        } else {
            sendMessage.setText("保存钱包信息成功"); // 替换原内容
        }

        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(message.getMessageId());
        try {
            bot.execute(deleteMessage);
            if (message.getReplyToMessage() != null) {
                deleteMessage.setMessageId(message.getReplyToMessage().getMessageId());
                bot.execute(deleteMessage);
            }
        } catch (TelegramApiException e) {
            log.error("chatId[{}]-messageId[{}]删除消息出错", chatId,deleteMessage.getMessageId(), e);
        }

        return sendMessage;
    }
}
