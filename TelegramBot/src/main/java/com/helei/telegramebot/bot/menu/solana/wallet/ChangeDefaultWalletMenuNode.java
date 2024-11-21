package com.helei.telegramebot.bot.menu.solana.wallet;

import cn.hutool.core.util.StrUtil;
import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import com.helei.telegramebot.entity.ChatWallet;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;



/**
 * 修改默认钱包菜单
 */
@Slf4j
public class ChangeDefaultWalletMenuNode extends AbstractWalletMenuNode {


    public ChangeDefaultWalletMenuNode(TGMenuNode parentMenu) {
        super(parentMenu,  "切换默认钱包", "CHANGE_DEFAULT_WALLET");
    }

    @Override
    public SendMessage buildDynamicMenu(String chatId) {
        return buildWalletSelectDynamicMenu("请点击键盘选择要更换的默认钱包", this, chatId);
    }

    @Override
    public SendMessage menuCommandHandler(MenuBaseTelegramBot bot, List<String> params, Message message) {
        String chatId = String.valueOf(message.getChatId());

        //Step 1 参数校验
        String pubKey = params.getFirst();
        if (StrUtil.isBlank(pubKey)) {
            log.error("chatId[{}] - [{}]命令参数为空", getButtonText(), chatId);
            return null;
        }

        //Step 2 公钥校验
        ChatWallet query = new ChatWallet();
        query.setPublicKey(pubKey);
        query.setChatId(chatId);

        ChatWallet chatWallet = persistenceService.queryChatIdWallet(query);
        if (chatWallet == null) {
            log.error("chatId[{}]不存在publicKey[{}]的钱包", chatId, pubKey);
            return null;
        }

        //Step 3 更新数据
        persistenceService.updateDefaultWalletAddress(bot.getBotUsername(), chatId, pubKey, true);


        //Step 4 删除之前的选择消息
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(message.getMessageId());
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("删除消息[{}}执行失败", deleteMessage);
        }

        //Step 5 发送消息提示
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format("默认钱包已更改为[%s]-[%s...]", chatWallet.getId(), pubKey.substring(0, 8)));
        return sendMessage;
    }
}
