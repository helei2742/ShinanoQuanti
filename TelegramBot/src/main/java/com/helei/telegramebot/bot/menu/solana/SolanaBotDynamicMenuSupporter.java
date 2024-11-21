package com.helei.telegramebot.bot.menu.solana;


import com.helei.telegramebot.entity.ChatWallet;
import com.helei.telegramebot.service.ISolanaATBotPersistenceService;
import com.helei.telegramebot.template.SolanaBotWalletTittleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SolanaBotDynamicMenuSupporter {

    private final String botUsername;

    private final ISolanaATBotPersistenceService persistenceService;

    public SolanaBotDynamicMenuSupporter(String botUsername, ISolanaATBotPersistenceService persistenceService) {
        this.botUsername = botUsername;
        this.persistenceService = persistenceService;
    }


//<==================================================================================   主菜单    ========================================================================================================================================>

    /**
     * 主菜单的tittle
     *
     * @param chatId chatId
     * @return tittle
     */
    public String mainMenuTittle(String chatId) {

        return "欢迎来到tg机器人";
    }

//<==================================================================================   设置    ========================================================================================================================================>

    /**
     * 设置菜单的tittle
     *
     * @param chatId chatId
     * @return tittle
     */
    public String settingMenuTittle(String chatId) {
        return "我的设置";
    }
//<==================================================================================   地址追踪    ========================================================================================================================================>

    /**
     * 追踪地址菜单标题
     *
     * @param chatId chatId
     * @return tittle
     */
    public String tranceAddressMenuTittle(String chatId) {
        return "地址追踪 - 未完成";
    }
//<==================================================================================   交易    ========================================================================================================================================>


    /**
     * 交易菜单标题
     *
     * @param chatId chatId
     * @return tittle
     */
    public String transactionMenuTittle(String chatId) {
        return "交易 - 未完成";
    }


//<==================================================================================   钱包    ========================================================================================================================================>

    /**
     * 钱包菜单标题
     *
     * @param chatId chatId
     * @return tittle
     */
    public String walletMenuTittle(String chatId) {
        //Step 1 查默认钱包公匙
        String pubKey = persistenceService.queryChatIdDefaultWalletAddress(botUsername, chatId);

        //Step 2 查询所有钱包
        List<ChatWallet> chatWallets = persistenceService.queryChatIdAllWallet(botUsername, chatId);

        //Step 3 找到默认钱包
        ChatWallet defaultWallet = dispatchDefaultWallet(chatWallets, pubKey);

        //Step 3 生成title

        return buildAllWalletInfoStr(defaultWallet, chatWallets);
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
        Optional<ChatWallet> first = chatWallets.stream().filter(c -> c.getPublicKey().equals(pubKey)).findFirst();
        if (first.isPresent()) {
            defaultWallet = first.get();
            chatWallets.remove(defaultWallet);
        }
        return defaultWallet;
    }

    /**
     * 构建所有钱包信息字符串
     *
     * @param defaultWallet defaultWallet
     * @param chatWallets   chatWallets
     * @return string
     */
    private static @NotNull String buildAllWalletInfoStr(ChatWallet defaultWallet, List<ChatWallet> chatWallets) {
        StringBuilder tittle = new StringBuilder();

        if (defaultWallet != null) {
            tittle.append("默认钱包:\n").append(SolanaBotWalletTittleTemplate.aChatWalletPrintStr(defaultWallet, defaultWallet.getId()));
        }

        tittle.append("其它钱包:\n");
        for (ChatWallet chatWallet : chatWallets) {
            tittle.append(SolanaBotWalletTittleTemplate.aChatWalletPrintStr(chatWallet, chatWallet.getId())).append("\n");
        }

        return tittle.toString();
    }
    /**
     * 绑定钱包菜单标题
     *
     * @param chatId chatId
     * @return tittle
     */
    public String walletBindWalletMenuTittle(String chatId) {
        return "绑定钱包";
    }

    /**
     * 创建钱包菜单标题
     *
     * @param chatId chatId
     * @return tittle
     */
    public String walletCreateWalletMenuTittle(String chatId) {
        return "创建钱包";
    }
}

