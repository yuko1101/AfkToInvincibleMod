package io.github.yuko1101.afktoinvincible.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AfkCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "afk";
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal(getCommandName())
                .then(literal("on")
                        .then(argument("player", StringArgumentType.string()).requires(source -> source.hasPermissionLevel(2))
                                .executes(
                                        ctx -> {
                                            final ServerCommandSource source = ctx.getSource();
                                            final String playerName = StringArgumentType.getString(ctx, "player");
                                            final ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
                                            AfkToInvincible.INSTANCE.serverManager.setAfkEnabled(player, true);

                                            source.sendFeedback(() -> Text.literal("Enabled AFK detection for " + playerName), false);
                                            return SINGLE_SUCCESS;
                                        }
                                )
                        )
                        .executes(
                            ctx -> {
                                final ServerCommandSource source = ctx.getSource();
                                if (source.getPlayer() == null) return SINGLE_SUCCESS;
                                AfkToInvincible.INSTANCE.serverManager.setAfkEnabled(source.getPlayer(), true);

                                source.sendFeedback(() -> Text.literal("Enabled AFK detection."), false);
                                return SINGLE_SUCCESS;
                            }
                        )
                )
                .then(literal("off")
                        .then(argument("player", StringArgumentType.string()).requires(source -> source.hasPermissionLevel(2))
                                .executes(
                                        ctx -> {
                                            final ServerCommandSource source = ctx.getSource();
                                            final String playerName = StringArgumentType.getString(ctx, "player");
                                            final ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
                                            AfkToInvincible.INSTANCE.serverManager.setAfkEnabled(player, false);

                                            source.sendFeedback(() -> Text.literal("Enabled AFK detection for " + playerName), false);
                                            return SINGLE_SUCCESS;
                                        }
                                )
                        )
                        .executes(
                                ctx -> {
                                    final ServerCommandSource source = ctx.getSource();
                                    if (source.getPlayer() == null) return SINGLE_SUCCESS;
                                    AfkToInvincible.INSTANCE.serverManager.setAfkEnabled(source.getPlayer(), false);

                                    source.sendFeedback(() -> Text.literal("Disabled AFK detection."), false);
                                    return SINGLE_SUCCESS;
                                }
                        )
                )
                .executes(
                        ctx -> {
                            final ServerCommandSource source = ctx.getSource();
                            final List<String> afkPlayers = AfkToInvincible.INSTANCE.serverManager.getAfkPlayers().stream().map(player -> player.getName().getString()).toList();

                            source.sendFeedback(() -> Text.literal("AFK: " + String.join(", ", afkPlayers)), false);

                            return SINGLE_SUCCESS;
                        }
                )
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        return SINGLE_SUCCESS;
    }
}
