package io.github.yuko1101.afktoinvincible.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class AfkCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "afk";
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal(getCommandName())
                .then(literal("on").executes(
                        ctx -> {
                            final ServerCommandSource source = ctx.getSource();
                            if (source.getPlayer() == null) return SINGLE_SUCCESS;
                            AfkToInvincibleServer.INSTANCE.setAfkEnabled(source.getPlayer(), true);

                            source.sendFeedback(() -> Text.literal("Enabled AFK detection."), false);
                            return SINGLE_SUCCESS;
                        }
                ))
                .then(literal("off").executes(
                        ctx -> {
                            final ServerCommandSource source = ctx.getSource();
                            if (source.getPlayer() == null) return SINGLE_SUCCESS;
                            AfkToInvincibleServer.INSTANCE.setAfkEnabled(source.getPlayer(), false);

                            source.sendFeedback(() -> Text.literal("Disabled AFK detection."), false);
                            return SINGLE_SUCCESS;
                        }
                ))
                .executes(
                        ctx -> {
                            final ServerCommandSource source = ctx.getSource();
                            final List<String> afkPlayers = AfkToInvincibleServer.INSTANCE.getAfkPlayers().stream().map(player -> player.getName().getString()).toList();

                            source.sendFeedback(() -> Text.literal("AFK: " + String.join(", ", afkPlayers)), false);

                            return SINGLE_SUCCESS;
                        }
                )
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return SINGLE_SUCCESS;
    }
}
