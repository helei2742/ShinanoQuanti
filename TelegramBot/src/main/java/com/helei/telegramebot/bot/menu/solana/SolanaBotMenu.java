package com.helei.telegramebot.bot.menu.solana;


import com.helei.dto.base.Result;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import com.helei.telegramebot.bot.menu.TelegramBotMenu;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.Serializable;

@Slf4j
public class SolanaBotMenu implements Serializable, TelegramBotMenu {

    private final String botUsername;

    private final ITelegramPersistenceService telegramPersistenceService;

    private final TGMenuNode menuRoot;

    public SolanaBotMenu(String botUsername, ITelegramPersistenceService telegramPersistenceService) {
        this.botUsername = botUsername;
        this.telegramPersistenceService = telegramPersistenceService;

        this.menuRoot = initMenuGraph();
    }


    @Override
    public SendMessage initChatMenu(String chatId) {
        Result result = telegramPersistenceService.saveChatMenuState(botUsername, chatId, menuRoot);

        if (result.getSuccess()) {
            Integer id = (Integer) result.getData();
            TGMenuNode tgMenuNode = TGMenuNode.nodeIdMap.get(id);

            return tgMenuNode.getMenu(chatId);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(result.getErrorMsg());
            return sendMessage;
        }
    }

    @Override
    public TGMenuNode getCurrentMenuState(String chatId) {
        Result result = telegramPersistenceService.getChatMenuState(botUsername, chatId);

        if (result.getSuccess()) {
            return TGMenuNode.nodeIdMap.get((Integer) result.getData());
        } else {
            return menuRoot;
        }
    }

    @Override
    public TGMenuNode getPrefer(String chatId) {

        return getCurrentMenuState(chatId).getParentMenu();
    }


    @Override
    public SendMessage getCurrentMenu(String chatId) {
        TGMenuNode currentMenu = getCurrentMenuState(chatId);

        SendMessage menu = currentMenu.getMenu(chatId);
        return menu;
    }

    @Override
    public SendMessage menuCommandHandler(String menuCommand, Message message) {

        TGMenuNode tgMenuNode = TGMenuNode.commandMap.get(menuCommand);
        log.info("收到菜单信息[{}], menuNode[{}]", menuCommand, tgMenuNode);

        return tgMenuNode.getMenu(String.valueOf(message.getChatId()));
    }



    private static TGMenuNode initMenuGraph() {
        // 1,主菜单
        TGMenuNode menuRoot = new TGMenuNode(null, "主菜单", "MAIN");

        // 2.钱包菜单
        TGMenuNode walletMenu = createWalletMenu(menuRoot);

        // 3.交易菜单
        TGMenuNode transaction = createTransactionMenu(menuRoot);

        // 4,追踪地址菜单
        TGMenuNode tranceAddress = createTranceAddressMenu(menuRoot);

        // 5.设置菜单
        TGMenuNode setting = createSettingMenu(menuRoot);


        menuRoot.getSubMenu().add(walletMenu);
        menuRoot.getSubMenu().add(transaction);
        menuRoot.getSubMenu().add(tranceAddress);
        menuRoot.getSubMenu().add(setting);

        return menuRoot;
    }

    private static TGMenuNode createSettingMenu(TGMenuNode menuRoot) {
        TGMenuNode setting = new TGMenuNode(menuRoot, "设置","SETTING");


        return setting;
    }

    private static TGMenuNode createTranceAddressMenu(TGMenuNode menuRoot) {
        TGMenuNode trance = new TGMenuNode(menuRoot, "跟踪地址", "TRANCE_ADDRESS");


        return trance;
    }

    private static TGMenuNode createTransactionMenu(TGMenuNode menuRoot) {
        TGMenuNode transactionMenu = new TGMenuNode(menuRoot, "交易", "TRANSACTION");

        return transactionMenu;
    }

    private static TGMenuNode createWalletMenu(TGMenuNode menuRoot) {
        TGMenuNode walletMenu = new TGMenuNode(menuRoot, "我的钱包", "WALLET");

        return walletMenu;
    }


    public static void main(String[] args) {
        System.out.println(initMenuGraph());
    }
}

