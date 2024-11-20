package com.helei.telegramebot.bot.menu;

import com.helei.dto.base.KeyValue;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public interface TelegramBotMenuType {

    String getName();

    SendMessage getMenu(String chatId);

    TelegramBotMenuType getPrefer();


    /**
     * 创建键盘行
     *
     * @param list 按钮text
     * @return list
     */
    default List<InlineKeyboardButton> createKeyboardRow(List<KeyValue<String, String>> list) {
        return list.stream().map(kv -> {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(kv.getKey());
            inlineKeyboardButton.setCallbackData("/menu." + kv.getValue());
            return inlineKeyboardButton;
        }).toList();
    }
}
