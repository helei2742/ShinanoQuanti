package com.helei.telegramebot.bot.menu.solana.wallet;

import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;


/**
 * 设置钱包名称菜单节点
 */
public class SetWalletNameMenuNode extends AbstractWalletMenuNode{


    public SetWalletNameMenuNode(TGMenuNode parentMenu) {
        super(parentMenu, "设置钱包名称", "SET_WALLET_NAME");
    }

    @Override
    public SendMessage buildDynamicMenu(String chatId) {
        return buildWalletSelectDynamicMenu("请选择希望设置名称的钱包", this, chatId);
    }

    @Override
    protected SendMessage menuCommandHandler(MenuBaseTelegramBot bot, List<String> params, Message message) {
        return null;
    }
}
