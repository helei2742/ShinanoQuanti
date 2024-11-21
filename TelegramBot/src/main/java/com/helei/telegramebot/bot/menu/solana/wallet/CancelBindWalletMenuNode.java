package com.helei.telegramebot.bot.menu.solana.wallet;

import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public class CancelBindWalletMenuNode extends AbstractWalletMenuNode{

    public CancelBindWalletMenuNode(TGMenuNode parentMenu) {
        super(parentMenu,"解除绑定", "CANCEL_BIND_WALLET");
    }

    @Override
    public SendMessage buildDynamicMenu(String chatId) {
        return buildWalletSelectDynamicMenu("请选择希望解除绑定的钱包\n注意,解绑操作无法撤回，请确保已保存好私匙,否则无法恢复钱包，可能会造成资产损失", this, chatId);
    }

    @Override
    protected SendMessage menuCommandHandler(MenuBaseTelegramBot bot, List<String> params, Message message) {
        return null;
    }
}

