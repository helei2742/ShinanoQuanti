package com.helei.telegramebot.bot.menu.solana;


import com.helei.dto.base.Result;
import com.helei.telegramebot.bot.impl.SolanaAutoTradeTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import com.helei.telegramebot.bot.menu.TelegramBotMenu;
import com.helei.telegramebot.bot.menu.solana.wallet.*;
import com.helei.telegramebot.service.ISolanaATBotPersistenceService;
import com.helei.telegramebot.service.ITelegramPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 机器人菜单
 */
@Slf4j
public class SolanaBotMenu implements Serializable, TelegramBotMenu {

    /**
     * 机器人的username
     */
    private final SolanaAutoTradeTelegramBot bot;

    /**
     * 持久化服务
     */
    private final ITelegramPersistenceService telegramPersistenceService;


    /**
     * 持久化服务
     */
    private final ISolanaATBotPersistenceService solanaATBotPersistenceService;


    /**
     * 标题创建相关的
     */
    private final SolanaBotDynamicMenuSupporter dynamicMenuSupporter;

    /**
     * 菜单根节点
     */
    private final TGMenuNode menuRoot;

    public SolanaBotMenu(SolanaAutoTradeTelegramBot bot, ITelegramPersistenceService telegramPersistenceService, ISolanaATBotPersistenceService solanaATBotPersistenceService) {
        this.bot = bot;
        this.telegramPersistenceService = telegramPersistenceService;
        this.solanaATBotPersistenceService = solanaATBotPersistenceService;
        this.dynamicMenuSupporter = new SolanaBotDynamicMenuSupporter(bot.getBotUsername(), solanaATBotPersistenceService);
        this.menuRoot = initMenuGraph();
    }


    @Override
    public SendMessage initChatMenu(String chatId) {
        Result result = telegramPersistenceService.saveChatMenuState(bot.getBotUsername(), chatId, menuRoot);

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
        Result result = telegramPersistenceService.getChatMenuState(bot.getBotUsername(), chatId);

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
    public BotApiMethod<?> menuCommandHandler(String menuCommand, Message message) {

        String[] split = menuCommand.split(" ");
        String command = split[0];
        List<String> params = new ArrayList<>();

        if (split.length > 1) {
            params.addAll(Arrays.asList(split).subList(1, split.length));
        }

        TGMenuNode tgMenuNode = TGMenuNode.commandMap.get(command);
        log.info("收到菜单信息[{}], params[{}] menuNode[{}]", command, params, tgMenuNode);

        BotApiMethod<?> sendMessage = null;
        if (!params.isEmpty()) {
            sendMessage = tgMenuNode.menuCommandHandler(params, message);
        } else {
            sendMessage = tgMenuNode.getMenu(String.valueOf(message.getChatId()));
        }

        telegramPersistenceService.saveChatMenuState(bot.getBotUsername(), String.valueOf(message.getChatId()), tgMenuNode);

        return sendMessage;
    }


    /**
     * 初始化菜单图
     *
     * @return TGMenuNode
     */
    private TGMenuNode initMenuGraph() {
        // 1,主菜单
        TGMenuNode menuRoot = new TGMenuNode(null, dynamicMenuSupporter::mainMenuTittle, "主菜单", "MAIN");

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

    /**
     * 创建设置菜单
     *
     * @param menuRoot menuRoot
     * @return TGMenuNode
     */
    private TGMenuNode createSettingMenu(TGMenuNode menuRoot) {
        TGMenuNode setting = new TGMenuNode(menuRoot, dynamicMenuSupporter::settingMenuTittle, "设置", "SETTING");


        return setting;
    }

    /**
     * 交易菜单
     *
     * @param menuRoot menuRoot
     * @return TGMenuNode
     */
    private TGMenuNode createTranceAddressMenu(TGMenuNode menuRoot) {
        TGMenuNode trance = new TGMenuNode(menuRoot, dynamicMenuSupporter::tranceAddressMenuTittle, "跟踪地址", "TRANCE_ADDRESS");


        return trance;
    }


    /**
     * 交易菜单
     *
     * @param menuRoot menuRoot
     * @return TGMenuNode
     */
    private TGMenuNode createTransactionMenu(TGMenuNode menuRoot) {
        TGMenuNode transactionMenu = new TGMenuNode(menuRoot, dynamicMenuSupporter::transactionMenuTittle, "交易", "TRANSACTION");

        return transactionMenu;
    }


    /**
     * 钱包菜单，包括钱包菜单下的子菜单
     *
     * @param menuRoot menuRoot
     * @return TGMenuNode
     */
    private TGMenuNode createWalletMenu(TGMenuNode menuRoot) {
        TGMenuNode walletMenu = new TGMenuNode(menuRoot, dynamicMenuSupporter::walletMenuTittle, "我的钱包", "WALLET");

        AbstractWalletMenuNode BIND_WALLET = new BindWalletMenuNode(walletMenu);
        AbstractWalletMenuNode CHANGE_DEFAULT_WALLET = new ChangeDefaultWalletMenuNode(walletMenu);

        AbstractWalletMenuNode SET_WALLET_NAME = new SetWalletNameMenuNode(walletMenu);
        AbstractWalletMenuNode CANCEL_BIND_WALLET = new CancelBindWalletMenuNode(walletMenu);
        AbstractWalletMenuNode EXPORT_PRIVATE_KEY = new ExportPrivateKeyMenuNode(walletMenu);
        AbstractWalletMenuNode SEND_SOL = new SendSolMenuNode(walletMenu);


        TGMenuNode CREATE_WALLET = new TGMenuNode(walletMenu, dynamicMenuSupporter::walletCreateWalletMenuTittle, "生成钱包", "CREATE_WALLET");

        BIND_WALLET.init(bot, solanaATBotPersistenceService);
        CHANGE_DEFAULT_WALLET.init(bot, solanaATBotPersistenceService);
        SET_WALLET_NAME.init(bot, solanaATBotPersistenceService);
        CANCEL_BIND_WALLET.init(bot, solanaATBotPersistenceService);
        EXPORT_PRIVATE_KEY.init(bot, solanaATBotPersistenceService);
        SEND_SOL.init(bot, solanaATBotPersistenceService);


        walletMenu.getSubMenu().add(CHANGE_DEFAULT_WALLET);
        walletMenu.getSubMenu().add(SET_WALLET_NAME);
        walletMenu.getSubMenu().add(BIND_WALLET);
        walletMenu.getSubMenu().add(CREATE_WALLET);
        walletMenu.getSubMenu().add(CANCEL_BIND_WALLET);
        walletMenu.getSubMenu().add(EXPORT_PRIVATE_KEY);
        walletMenu.getSubMenu().add(SEND_SOL);

        return walletMenu;
    }


}


