package com.helei.telegramebot.config.command;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface TelegramBotNameSpaceCommand {

    Set<String> getAllCommand();

    boolean isContainsCommand(String command);


    /**
     * 判断是否含有命令
     *
     * @param namespace namespace
     * @param command   command
     * @return  是否含有命令
     */
    static boolean isContainCommand(String namespace, String command) {
        try {
            NameSpace space = NameSpace.valueOf(namespace);
            return space.isContainsCommand(command);
        } catch (Exception e) {
            return false;
        }
    }

    enum NameSpace implements TelegramBotNameSpaceCommand {
        TRADE_SIGNAL_COMMAND {
            private final Set<String> commands = Arrays.stream(TradeSignalCommand.values()).map(TradeSignalCommand::name).collect(Collectors.toSet());

            @Override
            public Set<String> getAllCommand() {
                return new HashSet<>(commands);
            }

            @Override
            public boolean isContainsCommand(String command) {
                return commands.contains(command);
            }
        },
        SOLANA_BOT_COMMAND {
            private final Set<String> commands = Arrays.stream(SolanaBotCommand.values()).map(SolanaBotCommand::name).collect(Collectors.toSet());

            @Override
            public Set<String> getAllCommand() {
                return new HashSet<>(commands);
            }

            @Override
            public boolean isContainsCommand(String command) {
                return commands.contains(command);
            }
        }
    }
}

