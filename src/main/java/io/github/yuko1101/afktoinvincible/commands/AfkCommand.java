package io.github.yuko1101.afktoinvincible.commands;

import com.mojang.brigadier.context.CommandContext;
import io.github.yuko1101.afktoinvincible.server.AfkToInvincibleServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class AfkCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "afk";
    }
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();
        final List<String> afkPlayers = AfkToInvincibleServer.INSTANCE.getAfkPlayers().stream().map(player -> player.getName().getString()).toList();

        source.sendFeedback(Text.literal("AFK: " + String.join(", ", afkPlayers)), false);

        return SINGLE_SUCCESS;
    }
}
