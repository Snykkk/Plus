package com.marcusslover.plus.lib.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandManager {
    private final Set<org.bukkit.command.Command> commandSet = new HashSet<>();

    private static CommandManager instance;

    private CommandManager() {
        instance = this;
    }

    public static CommandManager get() {
        return instance == null ? new CommandManager() : instance;
    }

    public CommandManager register(@NotNull ICommand command) {
        return this.register("plus", command);
    }

    public CommandManager register(@NotNull String prefix, @NotNull ICommand command) {
        Command commandAnnotation = getCommandAnnotation(command);
        if (commandAnnotation == null) return this;
        String name = commandAnnotation.name();
        String description = commandAnnotation.description();
        List<String> aliases = Arrays.stream(commandAnnotation.aliases()).toList();

        CommandMap commandMap = Bukkit.getCommandMap();
        org.bukkit.command.Command cmd = new org.bukkit.command.Command(
                name, description, "", aliases) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                CommandContext commandContext = new CommandContext(sender, commandLabel, args);
                return command.execute(commandContext);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                TabCompleteContext tabCompleteContext = new TabCompleteContext(sender, args);
                return command.tab(tabCompleteContext);
            }
        };
        commandSet.add(cmd);
        commandMap.register(commandAnnotation.name(), prefix, cmd);
        return this;
    }

    public Set<org.bukkit.command.Command> getCommandSet() {
        return commandSet;
    }

    private @Nullable Command getCommandAnnotation(@NotNull ICommand command) {
        Class<? extends @NotNull ICommand> commandClass = command.getClass();
        Command[] annotationsByType = commandClass.getAnnotationsByType(Command.class);
        if (annotationsByType.length >= 1) {
            return annotationsByType[0];
        }
        return null;
    }
}
