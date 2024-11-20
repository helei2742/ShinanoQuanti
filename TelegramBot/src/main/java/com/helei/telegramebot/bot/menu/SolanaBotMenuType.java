package com.helei.telegramebot.bot.menu;

import com.helei.dto.base.KeyValue;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public enum SolanaBotMenuType implements TelegramBotMenuType {

    /**
     * 主菜单
     */
    MAIN {
        @Override
        public SendMessage getMenu(String chatId) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("欢迎使用TG机器人");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

            //第一行按钮
            keyboardRows.add(createKeyboardRow(List.of(
                    new KeyValue<>("钱包", "WAllET"),
                    new KeyValue<>("交易", "TRANSACTION")
            )));

            //第二行按钮
            keyboardRows.add(createKeyboardRow(List.of(
                    new KeyValue<>("钱包跟踪", "TRANCE_ADDRESS"),
                    new KeyValue<>("设置", "SETTING")
            )));

            markup.setKeyboard(keyboardRows);
            message.setReplyMarkup(markup);
            return message;
        }

        @Override
        public TelegramBotMenuType getPrefer() {
            return MAIN;
        }
    },

    /**
     * 钱包菜单
     */
    WALLET {
        @Override
        public SendMessage getMenu(String chatId) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("我的钱包");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

            //第一行按钮
            keyboardRows.add(createKeyboardRow(List.of(
                    new KeyValue<>("切换默认钱包", "切换默认钱包 TODO"),
                    new KeyValue<>("已绑定的钱包", "已绑定的钱包 TODO")
            )));

            //第二行按钮
            keyboardRows.add(createKeyboardRow(List.of(
                    new KeyValue<>("绑定钱包", "绑定钱包地址 TODO"),
                    new KeyValue<>("解除绑定", "解除绑定")
            )));

            keyboardRows.add(createKeyboardRow(List.of(
                    new KeyValue<>("返回", "/menu.back")
            )));

            markup.setKeyboard(keyboardRows);
            message.setReplyMarkup(markup);
            return message;
        }

        @Override
        public TelegramBotMenuType getPrefer() {
            return MAIN;
        }
    },

    /**
     * 交易菜单
     */
    TRANSACTION {
        @Override
        public SendMessage getMenu(String chatId) {


            return null;
        }

        @Override
        public TelegramBotMenuType getPrefer() {
            return MAIN;
        }
    },

    /**
     * 追踪地址菜单
     */
    TRANCE_ADDRESS {
        @Override
        public SendMessage getMenu(String chatId) {
            return null;
        }

        @Override
        public TelegramBotMenuType getPrefer() {
            return MAIN;
        }
    },

    /**
     * 设置菜单
     */
    SETTING {
        @Override
        public SendMessage getMenu(String chatId) {
            return null;
        }

        @Override
        public TelegramBotMenuType getPrefer() {
            return MAIN;
        }
    };


    public String getName() {
        return this.name();
    }
}

