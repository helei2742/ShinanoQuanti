package com.helei.telegramebot.bot.menu;

import com.google.common.collect.Lists;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class TGMenuNode {

    private static final AtomicInteger idGenerator = new AtomicInteger();

    public static final Map<Integer, TGMenuNode> nodeIdMap = new HashMap<>();

    public static final Map<String, TGMenuNode> commandMap = new HashMap<>();

    private final int id;

    private final TGMenuNode parentMenu;

    private final String title;

    private final String callbackData;

    private final List<TGMenuNode> subMenu = new ArrayList<>();

    public TGMenuNode(TGMenuNode parentMenu, String title, String callbackData) {
        this.parentMenu = parentMenu;
        this.title = title;
        this.callbackData = callbackData;
        this.id = idGenerator.incrementAndGet();

        nodeIdMap.put(id, this);

        commandMap.put(getCallbackData(), this);
    }

    public String getCallbackData() {
        if (parentMenu == null) {
            return "/menu." + callbackData;
        }
        return parentMenu.getCallbackData() + "." + callbackData;
    }

    public SendMessage getMenu(String chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(title);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        List<List<TGMenuNode>> partitions = Lists.partition(new ArrayList<>(subMenu), 2);

        for (List<TGMenuNode> partition : partitions) {
            keyboardRows.add(createKeyboardRow(partition));
        }

        if (parentMenu != null) {
            keyboardRows.add(createKeyboardRow(List.of(parentMenu)));
        }

        markup.setKeyboard(keyboardRows);
        message.setReplyMarkup(markup);
        return message;
    }

    List<InlineKeyboardButton> createKeyboardRow(List<TGMenuNode> list) {
        return list.stream().map(kv -> {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(kv.getTitle());
            inlineKeyboardButton.setCallbackData(kv.getCallbackData());
            return inlineKeyboardButton;
        }).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TGMenuNode that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TGMenuNode{" +
                "callbackData='" + callbackData + '\'' +
                ", title='" + title + '\'' +
                ", parentMenu=" + parentMenu +
                ", id=" + id +
                ", subMenu=" + subMenu.stream().map(TGMenuNode::getId).toList() +
                '}';
    }
}
