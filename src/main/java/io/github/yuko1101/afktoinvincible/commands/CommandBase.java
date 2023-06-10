package io.github.yuko1101.afktoinvincible.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public abstract class CommandBase {
    int SINGLE_SUCCESS = 1;

    abstract public String getCommandName();

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal(getCommandName()).executes(this::run));
    }

    abstract public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
}
