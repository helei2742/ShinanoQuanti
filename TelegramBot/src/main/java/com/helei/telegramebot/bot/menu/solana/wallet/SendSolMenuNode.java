package com.helei.telegramebot.bot.menu.solana.wallet;

import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public class SendSolMenuNode extends AbstractWalletMenuNode{

    public SendSolMenuNode(TGMenuNode parentMenu) {
        super(parentMenu,"转出sol", "SEND_SOL");
    }

    @Override
    public SendMessage buildDynamicMenu(String chatId) {
        return buildWalletSelectDynamicMenu("请选择希望转出sol的钱包", this, chatId);
    }

    @Override
    protected SendMessage menuCommandHandler(MenuBaseTelegramBot bot, List<String> params, Message message) {
        return null;
    }
}
