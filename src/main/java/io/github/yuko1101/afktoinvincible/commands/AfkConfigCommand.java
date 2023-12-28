package io.github.yuko1101.afktoinvincible.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.yuko1101.afktoinvincible.AfkToInvincible;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static io.github.yuko1101.afktoinvincible.AfkToInvincible.AFK_TICKS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AfkConfigCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "afkconfig";
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal(getCommandName()).requires(source -> source.hasPermissionLevel(2)).then(
                        literal("afkTimeout").then(
                                argument("timeout", IntegerArgumentType.integer(1)).executes(ctx -> setAfkTimeout(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "timeout")))
                        ).executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.literal("Current afk timeout is " + AFK_TICKS + " ticks."), false);
                            return SINGLE_SUCCESS;
                        })
                )
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {

        return SINGLE_SUCCESS;
    }

    public int setAfkTimeout(ServerCommandSource source, int timeout) {
        AfkToInvincible.AFK_TICKS = timeout;
        AfkToInvincibleServer.INSTANCE.sendAfkTimeoutPacketFromServer(AfkToInvincibleServer.INSTANCE.server.getPlayerManager().getPlayerList(), AFK_TICKS);
        AfkToInvincibleServer.configFile.set("afkTimeout", timeout);
        try {
            AfkToInvincibleServer.configFile.save();
        } catch (Exception ignored) { }

        // force remove afk invincible from all players
        AfkToInvincibleServer.afkTicksMap.clear();
        for (ServerPlayerEntity player : AfkToInvincibleServer.INSTANCE.server.getPlayerManager().getPlayerList()) {
            AfkToInvincibleServer.INSTANCE.updateInvincible(player, false, false);
        }

        source.sendFeedback(Text.literal("Set afk timeout to " + timeout + " ticks." + "\n" + "All afk players are no longer invincible to apply new timeout."), false);

        return SINGLE_SUCCESS;
    }
}
