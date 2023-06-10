package io.github.yuko1101.afktoinvincible.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class AfkCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "afk";
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();

        final List<String> afkPlayers = AfkToInvincibleServer.INSTANCE.getAfkPlayers().stream().map(player -> player.getName().getString()).toList();

        source.sendFeedback(() -> Text.literal("AFK: " + String.join(", ", afkPlayers)), false);

        return SINGLE_SUCCESS;
    }
}
