package com.helei.telegramebot.bot.menu.solana.wallet;

import com.helei.telegramebot.bot.MenuBaseTelegramBot;
import com.helei.telegramebot.bot.menu.TGMenuNode;
import com.helei.telegramebot.bot.menu.TGMenuNodeEnd;
import com.helei.telegramebot.entity.ChatWallet;
import com.helei.telegramebot.service.ISolanaATBotPersistenceService;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractWalletMenuNode extends TGMenuNodeEnd {


    private MenuBaseTelegramBot bot;

    protected ISolanaATBotPersistenceService persistenceService;

    public AbstractWalletMenuNode(
            TGMenuNode parentMenu,
            String buttonText,
            String callbackData
    ) {
        super(parentMenu, buttonText, callbackData);
    }

    @Override
    public BotApiMethod<?> menuCommandHandler(List<String> params, Message message) {
        return menuCommandHandler(bot, params, message);
    }

    protected abstract BotApiMethod<?> menuCommandHandler(MenuBaseTelegramBot bot, List<String> params, Message message);

    /**
     * 构建选择钱包的动态菜单
     *
     * @param text    文字
     * @param nodeEnd 菜单节点
     * @param chatId  chatId
     * @return 菜单
     */
    protected @NotNull SendMessage buildWalletSelectDynamicMenu(String text, TGMenuNodeEnd nodeEnd, String chatId) {
        SendMessage dynamicMenu = new SendMessage();
        dynamicMenu.setChatId(chatId);

        //Step 1 查默认钱包公匙
        String pubKey = persistenceService.queryChatIdDefaultWalletAddress(bot.getBotUsername(), chatId);

        //Step 2 查询所有钱包
        List<ChatWallet> chatWallets = persistenceService.queryChatIdAllWallet(bot.getBotUsername(), chatId);

        //Step 3 分出默认钱包
        dispatchDefaultWallet(chatWallets, pubKey);

        //Step 4 构建消息文字
        dynamicMenu.setText(text);

        //Step 5 构建动态键盘
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (int i = 0; i < chatWallets.size(); i++) {
            ChatWallet chatWallet = chatWallets.get(i);

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton("钱包 - " + i + 1);
            inlineKeyboardButton.setCallbackData(nodeEnd.getCallbackData() + " " + chatWallet.getPublicKey() + " end");
            keyboardRows.add(List.of(inlineKeyboardButton));
        }
        markup.setKeyboard(keyboardRows);
        dynamicMenu.setReplyMarkup(markup);
        return dynamicMenu;
    }

    /**
     * 分出默认钱包，会减少chatWallets
     *
     * @param chatWallets 钱包list
     * @param pubKey      默认钱包的公钥
     * @return 默认钱包
     */
    private ChatWallet dispatchDefaultWallet(List<ChatWallet> chatWallets, String pubKey) {
        ChatWallet defaultWallet = null;
        Optional<ChatWallet> first = chatWallets.stream().filter(c -> !pubKey.equals(c.getPublicKey())).findFirst();
        if (first.isPresent()) {
            defaultWallet = first.get();
            chatWallets.remove(defaultWallet);
        }
        return defaultWallet;
    }

    public void init(MenuBaseTelegramBot bot, ISolanaATBotPersistenceService solanaATBotPersistenceService) {
        this.bot = bot;
        this.persistenceService = solanaATBotPersistenceService;
    }
}
