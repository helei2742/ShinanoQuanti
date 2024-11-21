package com.helei.telegramebot.bot.menu;

import com.google.common.collect.Lists;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
        import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Data
public class TGMenuNode {

    private static final AtomicInteger idGenerator = new AtomicInteger();

    public static final Map<Integer, TGMenuNode> nodeIdMap = new HashMap<>();

    public static final Map<String, TGMenuNode> commandMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(TGMenuNode.class);

    private final int id;

    private final TGMenuNode parentMenu;

    private final Function<String, String> titleCreator;

    private final String buttonText;

    private final String callbackData;

    private final List<TGMenuNode> subMenu = new ArrayList<>();

    public TGMenuNode(TGMenuNode parentMenu, Function<String, String> titleCreator, String buttonText, String callbackData) {
        this.parentMenu = parentMenu;
        this.titleCreator = titleCreator;
        this.buttonText = buttonText;
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
        message.setText(titleCreator.apply(chatId));

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

    private List<InlineKeyboardButton> createKeyboardRow(List<TGMenuNode> list) {
        return list.stream().map(kv -> {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(kv.getButtonText());
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
                ", buttonText='" + buttonText + '\'' +
                ", parentMenu=" + parentMenu +
                ", id=" + id +
                ", subMenu=" + subMenu.stream().map(TGMenuNode::getId).toList() +
                '}';
    }

    public BotApiMethod<?> menuCommandHandler(List<String> params, Message message) {
        log.info("收到chatId[{}]菜单命令[{}] params[{}]", message.getChatId(), this, params);

        return null;
    }
}

